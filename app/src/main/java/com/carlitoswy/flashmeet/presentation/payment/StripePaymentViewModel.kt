package com.carlitoswy.flashmeet.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.data.model.PaymentRecord
import com.carlitoswy.flashmeet.data.repository.PaymentRepository
import com.carlitoswy.flashmeet.data.repository.SharedFlyerPaymentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StripePaymentViewModel @Inject constructor(
    private val sharedFlyerPaymentRepository: SharedFlyerPaymentRepository,
    private val paymentRepository: PaymentRepository,               // ✅ nuevo
    private val auth: FirebaseAuth,                                 // ✅ para userId
    private val functions: FirebaseFunctions                        // ✅ para llamar a la Function
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _statusText = MutableStateFlow("")
    val statusText: StateFlow<String> = _statusText

    // ⚠️ Ideal: mover a Remote Config / backend. Mantengo temporalmente para no romper tu flujo.
    val publishableKey: String =
        "pk_test_51KgF4kIHad7GoCUdlt2ifslj3nKHu4ad79MwhvKpQBVJrG8qRKE7z1fCfcYOdJPLpm8AT3fNelJkwWiL48STKu030006fulz3O"

    // Lo sigues observando en la UI (StripePaymentScreen)
    val clientSecret: StateFlow<String?> = sharedFlyerPaymentRepository.clientSecret

    private val defaultCurrency = "usd"
    private val defaultPurpose = "PROMOTION"

    /**
     * Llama a Firebase Functions -> createPaymentIntent
     * Actualiza clientSecret en SharedFlyerPaymentRepository
     * Registra en Firestore el PaymentRecord con estado "created"
     */
    fun fetchPaymentIntent() {
        viewModelScope.launch {
            _isLoading.value = true
            _statusText.value = "Creando PaymentIntent…"

            try {
                val flyer = sharedFlyerPaymentRepository.pendingFlyerData.value
                    ?: run {
                        _statusText.value = "No hay flyer pendiente para pago."
                        _isLoading.value = false
                        return@launch
                    }

                val userId = auth.currentUser?.uid ?: ""
                if (userId.isEmpty()) {
                    _statusText.value = "Usuario no autenticado."
                    _isLoading.value = false
                    return@launch
                }

                // ⚠️ Ajusta estos campos a tu modelo real de flyer:
                val amountCents = flyer.priceCents      // <- asegúrate de tenerlo en pendingFlyerData
                val eventId = flyer.eventId             // <- idem. Si no aplica, deja null

                if (amountCents != null) {
                    require(amountCents > 0) { "El monto debe ser > 0" }
                }

                // Payload para la Function (coincide con index.js)
                val payload = hashMapOf(
                    "amount" to (flyer.priceCents ?: 0L),
                    "currency" to defaultCurrency,
                    "purpose" to defaultPurpose,
                    "metadata" to mapOf(
                        "eventId" to (eventId ?: ""),
                        "userId" to userId
                    )
                )

                // Llamada real a createPaymentIntent (onCall)
                val res = functions
                    .getHttpsCallable("createPaymentIntent")
                    .call(payload)
                    .await()

                val data = res.data as? Map<*, *> ?: error("Respuesta inválida del backend")
                val fetchedSecret = data["clientSecret"] as? String ?: error("Sin clientSecret")
                val paymentId = (data["paymentId"] as? String)
                    ?: fetchedSecret.substringBefore("_secret_", missingDelimiterValue = "")

                // 1) exponemos el clientSecret a la UI
                sharedFlyerPaymentRepository.updateClientSecret(fetchedSecret)

                // 2) registramos el pago como "created" en Firestore
                if (amountCents != null) {
                    registerPaymentCreated(
                        paymentId = paymentId,
                        clientSecret = fetchedSecret,
                        userId = userId,
                        eventId = eventId,
                        amount = amountCents.toLong(),
                        currency = defaultCurrency,
                        purpose = defaultPurpose
                    )
                }

                _statusText.value = "PaymentIntent listo."
            } catch (e: Exception) {
                _statusText.value = "Error creando PaymentIntent: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda el registro inicial del pago (estado "created")
     */
    private fun registerPaymentCreated(
        paymentId: String,
        clientSecret: String,
        userId: String,
        eventId: String?,
        amount: Long,
        currency: String,
        purpose: String
    ) {
        viewModelScope.launch {
            runCatching {
                val record = PaymentRecord(
                    paymentId = paymentId,
                    clientSecret = clientSecret,
                    userId = userId,
                    eventId = eventId,
                    amount = amount,
                    currency = currency,
                    purpose = purpose,
                    status = "created"
                )
                paymentRepository.upsert(record)
            }.onFailure {
                // No bloqueamos el flujo si falla el guardado, pero lo dejamos logueado.
                _statusText.value = "Aviso: no se pudo registrar el pago (created): ${it.localizedMessage}"
            }
        }
    }

    /**
     * Actualiza el estado del pago y, si procede, marca el evento como "promoted".
     * Llamar desde el callback del PaymentSheet.
     */
    fun updatePaymentStatus(paymentId: String, status: String, eventId: String?) {
        viewModelScope.launch {
            runCatching {
                paymentRepository.updateStatus(paymentId, status)
                if (status == "succeeded" && !eventId.isNullOrBlank()) {
                    paymentRepository.markEventPromoted(eventId, paymentId)
                }
            }.onFailure {
                _statusText.value = "Aviso: no se pudo actualizar estado del pago ($status): ${it.localizedMessage}"
            }
        }
    }

    fun setStatus(message: String) {
        _statusText.value = message
    }

    fun finalizeFlyerPaymentSuccess(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Limpia el estado del flyer pendiente y notifica a la UI
            sharedFlyerPaymentRepository.clearPendingFlyer()
            onSuccess()
        }
    }

    fun observePayment(paymentId: String) = paymentRepository.observePayment(paymentId)
}
