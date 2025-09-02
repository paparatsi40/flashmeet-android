// app/src/main/java/.../data/model/PaymentRecord.kt
package com.carlitoswy.flashmeet.data.model

import com.google.firebase.Timestamp

data class PaymentRecord(
    val paymentId: String,           // Stripe PaymentIntent id (pi_xxx)
    val clientSecret: String,        // opcional para auditoría
    val userId: String,
    val eventId: String?,            // si aplica, ej. promo de un evento
    val amount: Long,                // en centavos
    val currency: String = "usd",
    val purpose: String = "PROMOTION",
    val status: String = "created",  // created|processing|succeeded|canceled|failed
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
