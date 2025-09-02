// app/src/main/java/com/carlitoswy/flashmeet/data/repository/PaymentRepository.kt
package com.carlitoswy.flashmeet.data.repository

import com.carlitoswy.flashmeet.data.model.PaymentRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    private val payments = db.collection("payments")
    private val events = db.collection("events")

    /**
     * Crea o actualiza un PaymentRecord usando serverTimestamp para updatedAt.
     * Si no existe createdAt, lo define (solo primera vez).
     */
    suspend fun upsert(record: PaymentRecord) {
        val ref = payments.document(record.paymentId)
        val snap = ref.get(Source.SERVER).await()

        val base = mutableMapOf<String, Any?>(
            "paymentId" to record.paymentId,
            "clientSecret" to record.clientSecret,
            "userId" to record.userId,
            "eventId" to record.eventId,
            "amount" to record.amount,
            "currency" to record.currency,
            "purpose" to record.purpose,
            "status" to record.status,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // Si el doc no existe, setear createdAt con serverTimestamp
        if (!snap.exists()) {
            base["createdAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
        }

        ref.set(base, SetOptions.merge()).await()
    }

    /**
     * Actualiza solo el status + updatedAt (serverTimestamp).
     */
    suspend fun updateStatus(paymentId: String, status: String) {
        payments.document(paymentId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    /**
     * Marca el evento como promovido y referencia el último paymentId.
     * Útil post-éxito o desde webhook.
     */
    suspend fun markEventPromoted(eventId: String, paymentId: String) {
        events.document(eventId)
            .set(
                mapOf(
                    "promoted" to true,
                    "lastPaymentId" to paymentId,
                    "promotedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    /**
     * Operación atómica (una sola transacción) para:
     * - actualizar estado del pago
     * - marcar evento como promovido (si aplica)
     */
    suspend fun updateStatusAndPromoteIfNeeded(
        paymentId: String,
        status: String,
        eventId: String?
    ) {
        db.runTransaction { tx ->
            val payRef = payments.document(paymentId)
            tx.update(
                payRef,
                mapOf(
                    "status" to status,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )

            if (status == "succeeded" && !eventId.isNullOrBlank()) {
                val evRef = events.document(eventId)
                tx.set(
                    evRef,
                    mapOf(
                        "promoted" to true,
                        "lastPaymentId" to paymentId,
                        "promotedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
        }.await()
    }

    /**
     * Consulta de los pagos del usuario autenticado (para listas).
     * Requiere índice por createdAt si mezclas where + orderBy.
     */
    fun myPaymentsQuery() =
        payments.whereEqualTo("userId", auth.currentUser?.uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)

    /**
     * Flow en tiempo real de un pago concreto.
     */
    fun observePayment(paymentId: String): Flow<PaymentRecord?> = callbackFlow {
        val reg = payments.document(paymentId).addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snap?.toPaymentRecord())
        }
        awaitClose { reg.remove() }
    }

    /**
     * Lectura puntual (no reactiva) de un pago.
     */
    suspend fun getPaymentOnce(paymentId: String): PaymentRecord? {
        val snap = payments.document(paymentId).get(Source.SERVER).await()
        return snap.toPaymentRecord()
    }

    /** Mapping helper */
    private fun DocumentSnapshot.toPaymentRecord(): PaymentRecord? {
        val obj = this.toObject<PaymentRecord>() ?: return null
        // createdAt/updatedAt pueden venir null si aún no se resuelven los serverTimestamp
        // opcionalmente podrías forzar valores locales si necesitas.
        return obj
    }
}
