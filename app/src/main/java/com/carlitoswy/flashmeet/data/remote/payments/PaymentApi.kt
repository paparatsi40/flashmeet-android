package com.carlitoswy.flashmeet.data.remote.payments

import retrofit2.http.Body
import retrofit2.http.POST

data class StripePaymentRequest(
    val amount: Long,
    val currency: String = "usd",
    val purpose: String? = null,                 // "PROMOTION" | "BANNER" (opcional)
    val metadata: Map<String, String>? = null    // e.g., eventId, userId
)

data class StripePaymentResponse(
    val clientSecret: String,
    val paymentId: String? = null                // si tu backend lo devuelve
)

interface PaymentApi {
    @POST("/create-payment-intent")
    suspend fun createPaymentIntent(@Body req: StripePaymentRequest): StripePaymentResponse
}
