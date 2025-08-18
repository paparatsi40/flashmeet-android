package com.carlitoswy.flashmeet.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.presentation.shared.PendingEventData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class PaymentCompletionViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    /**
     * Activa promoción tras el pago de forma atómica:
     * - Si no hay evento, lo crea destacado.
     * - Si ya existe, lo marca como destacado.
     * - Registra la promoción vinculada al evento.
     * Retorna el eventId resultante.
     */
    fun activatePromotionAfterPayment(
        pending: PendingEventData,
        paymentId: String?,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                val now = Instant.now()
                val endsAt = promoEnd(now, pending.adOption.name)

                val resultEventId = db.runTransaction { txn ->
                    val events = db.collection("events")
                    val eventId = pending.id ?: events.document().id
                    val eventRef = events.document(eventId)

                    if (pending.id == null) {
                        // Crear evento
                        val data = mapOf(
                            "id" to eventId,
                            "ownerId" to pending.userId,
                            "title" to pending.title,
                            "description" to pending.description,
                            "locationName" to pending.locationName,
                            "createdAt" to now.toEpochMilli(),
                            "isFeatured" to true,
                            "featuredUntil" to endsAt.toEpochMilli(),
                            "adOption" to pending.adOption.name
                        )
                        txn.set(eventRef, data)
                    } else {
                        // Actualizar evento existente
                        txn.update(
                            eventRef,
                            mapOf(
                                "isFeatured" to true,
                                "featuredUntil" to endsAt.toEpochMilli(),
                                "adOption" to pending.adOption.name
                            )
                        )
                    }

                    // Crear promoción
                    val promoRef = db.collection("promotions").document()
                    val promoData = mapOf(
                        "id" to promoRef.id,
                        "eventId" to eventId,
                        "ownerId" to pending.userId,
                        "plan" to pending.adOption.name, // ajusta si usas otro mapping
                        "startsAt" to now.toEpochMilli(),
                        "endsAt" to endsAt.toEpochMilli(),
                        "paymentId" to (paymentId ?: ""),
                        "active" to true
                    )
                    txn.set(promoRef, promoData)

                    // devuelve el id del evento desde la transacción
                    eventId
                }.await()

                resultEventId
            }.onSuccess { eventId ->
                onResult(Result.success(eventId))
            }.onFailure { e ->
                onResult(Result.failure(e))
            }
        }
    }

    private fun promoEnd(now: Instant, planName: String): Instant = when (planName) {
        "PREMIUM" -> now.plusSeconds(7 * 86_400)    // 7 días
        "FEATURED" -> now.plusSeconds(3 * 86_400)   // 3 días
        else -> now.plusSeconds(1 * 86_400)         // fallback 1 día
    }
}
