package com.carlitoswy.flashmeet.data.repository

import android.util.Log
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.carlitoswy.flashmeet.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {

    private val TAG = "EventRepo"

    override fun getMyEvents(): Flow<List<Event>> = callbackFlow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        val subscription = firestore.collection("events")
            .whereEqualTo("createdBy", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                } else {
                    val events = snapshot?.toObjects(Event::class.java).orEmpty()
                    Log.d(TAG, "getMyEvents → ${events.size} eventos obtenidos")
                    trySend(events)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createEvent(event: Event) {
        firestore.collection("events").document(event.id).set(event).await()
        Log.d(TAG, "Evento creado: ${event.id}")
    }

    override fun getNearbyEvents(latitude: Double, longitude: Double): Flow<List<Event>> = callbackFlow {
        val query = firestore.collection("events")
            .whereGreaterThanOrEqualTo("latitude", latitude - 0.1)
            .whereLessThanOrEqualTo("latitude", latitude + 0.1)
            .whereGreaterThanOrEqualTo("longitude", longitude - 0.1)
            .whereLessThanOrEqualTo("longitude", longitude + 0.1)

        val subscription = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val events = snapshot?.toObjects(Event::class.java).orEmpty()
            Log.d(TAG, "getNearbyEvents → ${events.size} eventos obtenidos cerca de $latitude,$longitude")
            trySend(events)
        }

        awaitClose { subscription.remove() }
    }

    override suspend fun searchEvents(
        country: String,
        city: String,
        postal: String,
        category: String,
        date: String
    ): List<Event> {
        return try {
            var query: Query = firestore.collection("events")

            if (country.isNotEmpty()) query = query.whereEqualTo("country", country)
            if (city.isNotEmpty()) query = query.whereEqualTo("city", city)
            if (postal.isNotEmpty()) query = query.whereEqualTo("postal", postal)
            if (category.isNotEmpty() && category != "Todos") query = query.whereEqualTo("category", category)

            // ✅ Filtro por fecha si se proporciona
            if (date.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = dateFormat.parse(date)

                    val calendar = Calendar.getInstance().apply {
                        time = selectedDate!!
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val startOfDay = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val endOfDay = calendar.timeInMillis

                    query = query
                        .orderBy("timestamp")
                        .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                        .whereLessThan("timestamp", endOfDay)

                    Log.d(TAG, "Filtro fecha → $startOfDay a $endOfDay")
                } catch (e: ParseException) {
                    Log.w(TAG, "Error parseando fecha: $date → ${e.message}")
                }
            }

            val snapshot = query.get().await()
            val events = snapshot.documents.mapNotNull { it.toObject(Event::class.java) }

            Log.d(TAG, "searchEvents → ${events.size} eventos encontrados con filtros: " +
                    "[country=$country, city=$city, postal=$postal, category=$category, date=$date]")

            events
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando eventos: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return try {
            firestore.collection("events").document(eventId)
                .get().await()
                .toObject(Event::class.java).also {
                    Log.d(TAG, "getEventById($eventId) → ${it != null}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo evento $eventId: ${e.message}")
            null
        }
    }

    override suspend fun updateEvent(event: Event) {
        try {
            firestore.collection("events").document(event.id).set(event).await()
            Log.d(TAG, "Evento actualizado: ${event.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando evento ${event.id}: ${e.message}")
            throw e
        }
    }
}
