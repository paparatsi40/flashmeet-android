package com.carlitoswy.flashmeet.presentation.payment

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.carlitoswy.flashmeet.presentation.shared.SharedEventStateViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import java.util.UUID

@Composable
fun PaymentSummaryScreen(
    navController: NavHostController,
    sharedEventStateViewModel: SharedEventStateViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val pendingEvent = sharedEventStateViewModel.pendingEvent.collectAsState().value
    if (pendingEvent == null) {
        Text("❌ No hay datos de evento para procesar.")
        return
    }

    // --- UI state del pago
    val clientSecret by paymentViewModel.clientSecret.collectAsState()
    val isLoading by paymentViewModel.isLoading.collectAsState()
    val error by paymentViewModel.error.collectAsState()

    // --- Contexto y PaymentSheet
    val context = LocalContext.current
    val activity = remember(context) { context as Activity as ComponentActivity }

    var paymentSheet by remember { mutableStateOf<PaymentSheet?>(null) }
    LaunchedEffect(Unit) {
        // Stripe ya se inicializa en FlashMeetApp con PaymentConfiguration.init(...)
        paymentSheet = PaymentSheet(activity) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    // TODO: aquí puedes crear/actualizar la promoción o banner si no lo haces por webhook.
                    navController.popBackStack()
                    paymentViewModel.clear()
                }
                is PaymentSheetResult.Canceled -> { /* usuario canceló */ }
                is PaymentSheetResult.Failed -> { /* mostrar error si deseas */ }
            }
        }
    }

    val sheetConfig = remember {
        PaymentSheet.Configuration(
            merchantDisplayName = "FlashMeet"
            // , googlePay = PaymentSheet.GooglePayConfiguration(
            //     environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
            //     countryCode = "US",
            //     currencyCode = "USD"
            // )
        )
    }

    // Presenta PaymentSheet cuando llegue el clientSecret
    LaunchedEffect(clientSecret) {
        clientSecret?.let { cs -> paymentSheet?.presentWithPaymentIntent(cs, sheetConfig) }
    }

    // --- Cálculo de monto
    val costText = when (pendingEvent.adOption.name) {
        "PREMIUM" -> "$7.99"
        "FEATURED" -> "$3.99"
        else -> "$0.00"
    }
    val amountCents = when (pendingEvent.adOption.name) {
        "PREMIUM" -> 799L
        "FEATURED" -> 399L
        else -> 0L
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resumen de Pago", style = MaterialTheme.typography.headlineSmall)

        Text("📌 Título: ${pendingEvent.title}")
        Text("📝 Descripción: ${pendingEvent.description}")
        Text("📍 Ubicación: ${pendingEvent.locationName}")
        Text("🏷️ Opción de Publicidad: ${pendingEvent.adOption}")
        Text("⭐ Texto Destacado: ${pendingEvent.highlightedText.ifBlank { "N/A" }}")
        Text("💰 Costo estimado: $costText")

        Spacer(Modifier.height(8.dp))

        if (amountCents == 0L) {
            Text("Este plan no requiere pago.")
            Button(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        } else {
            Button(
                enabled = !isLoading,
                onClick = {
                    // Construye metadata segura: incluye eventId solo si existe
                    val metadata = buildMap {
                        put("userId", pendingEvent.userId)
                        put("adOption", pendingEvent.adOption.name)
                        // Identificador de borrador para correlación en backend si aún no hay eventId
                        put("draftId", UUID.randomUUID().toString())
                        pendingEvent.id?.let { put("eventId", it) }
                    }

                    paymentViewModel.requestPaymentIntent(
                        amountCents = amountCents,
                        currency = "usd",
                        purpose = "PROMOTION",
                        metadata = metadata
                    )
                }
            ) {
                Text(if (isLoading) "Preparando pago..." else "Pagar ahora")
            }
        }

        error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
    }
}
