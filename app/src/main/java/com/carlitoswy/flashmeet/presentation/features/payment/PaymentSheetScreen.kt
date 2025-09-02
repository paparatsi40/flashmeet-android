package com.carlitoswy.flashmeet.presentation.payment

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
fun PaymentSheetScreen(
    viewModel: PaymentViewModel,
    amountCents: Long,
    eventId: String,
    currency: String = "usd",
    onCompleted: (paymentId: String?) -> Unit,
    onCanceled: () -> Unit,
) {
    val uiClientSecret by viewModel.clientSecret.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val activity = remember(context) { context as Activity as ComponentActivity }

    // Instancia PaymentSheet con callback
    var paymentSheet by remember { mutableStateOf<PaymentSheet?>(null) }
    LaunchedEffect(Unit) {
        paymentSheet = PaymentSheet(activity) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    onCompleted(null) // si tu backend devuelve paymentId, pásalo
                    viewModel.clear()
                }
                is PaymentSheetResult.Canceled -> onCanceled()
                is PaymentSheetResult.Failed -> {
                    // podrías mapear a un snackbar externo
                }
            }
        }
    }

    // Configuración de la hoja
    val sheetConfig = remember {
        PaymentSheet.Configuration(
            merchantDisplayName = "FlashMeet"
            // Activa Google Pay si lo necesitas:
            // , googlePay = PaymentSheet.GooglePayConfiguration(
            //     environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
            //     countryCode = "US",
            //     currencyCode = currency.uppercase()
            // )
        )
    }

    // Presenta hoja cuando llegue el clientSecret
    LaunchedEffect(uiClientSecret) {
        uiClientSecret?.let { cs ->
            paymentSheet?.presentWithPaymentIntent(cs, sheetConfig)
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Pagar promoción", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = !isLoading,
            onClick = {
                viewModel.requestPaymentIntent(
                    amountCents = amountCents,
                    currency = currency,
                    purpose = "PROMOTION",
                    metadata = mapOf("eventId" to eventId)
                )
            }
        ) {
            val dollars = amountCents / 100
            val cents = (amountCents % 100).toString().padStart(2, '0')
            Text(if (isLoading) "Preparando pago..." else "Pagar \$$dollars.$cents")
        }
        error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
    }
}
