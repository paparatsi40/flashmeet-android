package com.carlitoswy.flashmeet.presentation.payment.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlitoswy.flashmeet.presentation.payment.StripePaymentViewModel
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun PaymentStatusBanner(
    paymentId: String,
    vm: StripePaymentViewModel
) {
    // Observa el pago en tiempo real vía VM
    val payment = vm.observePayment(paymentId)
        .filterNotNull()
        .collectAsState(initial = null).value

    if (payment != null) {
        val text = when (payment.status) {
            "succeeded" -> "✅ Pago exitoso"
            "processing" -> "⏳ Procesando pago…"
            "requires_payment_method" -> "⚠️ Requiere otro método de pago"
            "canceled" -> "⚠️ Pago cancelado"
            "failed" -> "❌ Pago fallido"
            else -> "ℹ️ Estado: ${payment.status}"
        }
        Surface(tonalElevation = 2.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text)
                // Si quieres un botón "Ver": TextButton({ }) { Text("Ver") }
            }
        }
    }
}
