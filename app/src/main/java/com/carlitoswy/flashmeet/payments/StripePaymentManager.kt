package com.carlitoswy.flashmeet.payments

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

object StripePaymentManager {

    private const val TAG = "StripePaymentManager"

    // Opcional: si usas Functions en una región específica:
    private val functions by lazy {
        // FirebaseFunctions.getInstance("us-central1")
        FirebaseFunctions.getInstance()
    }

    /**
     * Inicializa Stripe. Idealmente pasa la publishableKey desde backend/RemoteConfig.
     * Nunca la hardcodees en release.
     */
    fun init(context: Context, publishableKey: String) {
        PaymentConfiguration.init(context, publishableKey)
        Log.d(TAG, "Stripe init OK")
    }

    // =========================================
    // PaymentSheet (recomendado)
    // =========================================

    /**
     * Presenta PaymentSheet inmediatamente (one-shot).
     * Si quieres reutilizar, crea una instancia a nivel de Activity/VM.
     */
    fun presentPaymentSheet(
        activity: ComponentActivity,
        clientSecret: String,
        configuration: PaymentSheet.Configuration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        // Crea PaymentSheet con callback; presenta con el clientSecret
        val sheet = PaymentSheet(activity) { result -> onResult(result) }
        sheet.presentWithPaymentIntent(clientSecret, configuration)
    }

    /**
     * Crea un PaymentIntent en el backend.
     * Incluye metadata e idempotencyKey para reintentos seguros.
     */
    suspend fun createPaymentIntent(
        amountCents: Long,
        currency: String,
        purpose: String,                 // e.g. "PROMOTION" | "BANNER"
        metadata: Map<String, String> = emptyMap(),
        functionName: String = "createPaymentIntent" // usa "payments-createPaymentIntent" si migras
    ): CreatePIResponse = withContext(Dispatchers.IO) {
        require(amountCents > 0) { "amountCents must be > 0" }

        val payload = hashMapOf(
            "amount" to amountCents,              // smallest unit
            "currency" to currency.lowercase(),
            "purpose" to purpose,
            "metadata" to (metadata + mapOf("idempotencyKey" to UUID.randomUUID().toString()))
        )

        runCatching {
            val res: HttpsCallableResult = functions
                .getHttpsCallable(functionName)
                .call(payload)
                .await()

            val map = res.data as Map<*, *>
            val clientSecret = map["clientSecret"] as? String
                ?: error("Missing clientSecret")
            val paymentId = map["paymentId"] as? String
                ?: (map["id"] as? String ?: "")

            CreatePIResponse.Success(paymentId, clientSecret)
        }.getOrElse { e ->
            Log.e(TAG, "Error createPaymentIntent: ${e.message}", e)
            CreatePIResponse.Error(e)
        }
    }

    // =========================================
    // Fallback: Confirmación manual (no recomendado para nuevos flujos)
    // =========================================

    /**
     * Si decides mantener confirmación manual con tarjeta de prueba.
     * OJO: en prod usa PaymentSheet/PaymentLauncher.
     */
    suspend fun confirmManualCardPayment(
        activity: ComponentActivity,
        clientSecret: String,
        params: com.stripe.android.model.PaymentMethodCreateParams
    ): ManualConfirmResult = withContext(Dispatchers.Main) {
        try {
            val stripe = com.stripe.android.Stripe(
                activity,
                PaymentConfiguration.getInstance(activity).publishableKey
            )

            val confirmParams = com.stripe.android.model.ConfirmPaymentIntentParams
                .createWithPaymentMethodCreateParams(
                    paymentMethodCreateParams = params,
                    clientSecret = clientSecret
                )

            // Esto inicia un flujo que puede requerir 3DS y enviará resultado a onActivityResult (SDK maneja internamente).
            // Necesitas observar el resultado (PaymentAuth UI). Con PaymentSheet no hace falta este manejo.
            stripe.confirmPayment(activity, confirmParams)
            ManualConfirmResult.Launched // El resultado final lo recibirá el SDK en la Activity.
        } catch (e: Exception) {
            Log.e(TAG, "Manual confirm error: ${e.message}", e)
            ManualConfirmResult.Error(e)
        }
    }

    /** Útil para pruebas locales sin tocar Stripe */
    suspend fun simulateTestPayment(clientSecret: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Simulando pago con clientSecret: $clientSecret")
        true
    }
}

/** Respuesta tipada para la creación del PaymentIntent */
sealed interface CreatePIResponse {
    data class Success(val paymentId: String, val clientSecret: String) : CreatePIResponse
    data class Error(val error: Throwable) : CreatePIResponse
}

/** Resultado del flujo manual */
sealed interface ManualConfirmResult {
    data object Launched : ManualConfirmResult
    data class Error(val error: Throwable) : ManualConfirmResult
}
