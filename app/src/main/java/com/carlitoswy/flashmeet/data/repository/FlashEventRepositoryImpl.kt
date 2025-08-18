package com.carlitoswy.flashmeet.data.repository

import com.carlitoswy.flashmeet.domain.model.FlashEvent
import com.carlitoswy.flashmeet.domain.repository.FlashEventRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FlashEventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FlashEventRepository {

    private val eventsCollection = firestore.collection("events")

    override suspend fun createEvent(event: FlashEvent): Result<Unit> {
        return try {
            eventsCollection.document(event.id).set(event).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<List<FlashEvent>> = callbackFlow {
        val listener = eventsCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FlashEvent>()
            }.filter { event ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    latitude, longitude,
                    event.latitude, event.longitude,
                    results
                )
                results[0] <= radiusInKm * 1000
            }

            trySend(events)
        }

        awaitClose { listener.remove() }
    }
}
