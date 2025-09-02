package com.carlitoswy.flashmeet.data.repository

import android.net.Uri
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : EventRepository {

    private val eventsCollection = firestore.collection("events")

    override fun getMyEvents(): Flow<List<Event>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = eventsCollection
            .whereEqualTo("createdBy", uid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snap?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)
                    } catch (e: Exception) {
                        println("⚠️ Error deserializing event: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }


    override fun getNearbyEvents(latitude: Double, longitude: Double): Flow<List<Event>> = callbackFlow {
        val query = eventsCollection
            .whereGreaterThanOrEqualTo("latitude", latitude - 0.1)
            .whereLessThanOrEqualTo("latitude", latitude + 0.1)
            .whereGreaterThanOrEqualTo("longitude", longitude - 0.1)
            .whereLessThanOrEqualTo("longitude", longitude + 0.1)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val events = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Event::class.java)
                } catch (e: Exception) {
                    println("⚠️ Error deserializing nearby event: ${e.message}")
                    null
                }
            }.orEmpty()

            trySend(events)
        }

        awaitClose { listener.remove() }
    }


    override suspend fun createEvent(event: Event) {
        eventsCollection.document(event.id).set(event).await()
    }

    override suspend fun searchEvents(
        country: String,
        city: String,
        postal: String,
        category: String,
        date: String
    ): List<Event> {
        return try {
            var query: Query = eventsCollection

            if (country.isNotEmpty()) {
                query = query.whereEqualTo("country", country)
            }
            if (city.isNotEmpty()) {
                query = query.whereEqualTo("city", city)
            }
            if (postal.isNotEmpty()) {
                query = query.whereEqualTo("postal", postal)
            }

            if (category.isNotEmpty() && category != "Todos") {
                query = query.whereEqualTo("category", category)
            }

            if (date.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = dateFormat.parse(date)

                    val calendar = Calendar.getInstance().apply {
                        time = selectedDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfDayMillis = calendar.timeInMillis

                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val endOfDayMillis = calendar.timeInMillis

                    query = query
                        .orderBy("timestamp")
                        .whereGreaterThanOrEqualTo("timestamp", startOfDayMillis)
                        .whereLessThan("timestamp", endOfDayMillis)
                } catch (e: ParseException) {
                    println("FlashMeet: Error parsing date: $date. ${e.message}")
                }
            }

            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Event::class.java)
                } catch (e: Exception) {
                    println("⚠️ Error deserializing searched event: ${e.message}")
                    null
                }
            }

        } catch (e: Exception) {
            println("FlashMeet: Error searching events: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return try {
            val snap = eventsCollection.document(eventId).get().await()
            try {
                snap.toObject(Event::class.java)
            } catch (e: Exception) {
                println("⚠️ Error deserializing event by ID: ${e.message}")
                null
            }

        } catch (e: Exception) {
            println("FlashMeet: Error getting event by ID $eventId: ${e.message}")
            null
        }
    }

    override suspend fun updateEvent(event: Event) {
        try {
            eventsCollection.document(event.id).set(event).await()
        } catch (e: Exception) {
            println("FlashMeet: Error updating event ${event.id}: ${e.message}")
            throw e
        }
    }

    override suspend fun uploadEventImage(imageUri: Uri, eventId: String): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance()
                .reference
                .child("event_images/$eventId.jpg")

            val uploadTask = storageRef.putFile(imageUri).await()
            if (uploadTask.task.isSuccessful) {
                storageRef.downloadUrl.await().toString()
            } else {
                println("FlashMeet: Error uploading image: ${uploadTask.task.exception?.message}")
                null
            }
        } catch (e: Exception) {
            println("FlashMeet: Exception in uploadEventImage: ${e.message}")
            null
        }
    }
}
