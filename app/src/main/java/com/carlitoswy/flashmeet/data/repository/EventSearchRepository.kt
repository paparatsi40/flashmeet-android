package com.carlitoswy.flashmeet.data.repository

import com.carlitoswy.flashmeet.domain.model.Flyer
import com.carlitoswy.flashmeet.utils.LatLng
import com.carlitoswy.flashmeet.utils.boundingBox
import com.carlitoswy.flashmeet.utils.haversineKm
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventSearchRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun searchFlyers(
        center: LatLng,
        radiusKm: Double,
        text: String?,
        fromDateMillis: Long?,
        toDateMillis: Long?,
        promotedOnly: Boolean
    ): List<Flyer> {
        val (sw, ne) = boundingBox(center, radiusKm)
        var q: Query = db.collection("flyers")
            .whereGreaterThanOrEqualTo("location.latitude", sw.lat)
            .whereLessThanOrEqualTo("location.latitude", ne.lat)
            // Nota: Firestore no permite dos desigualdades en distintos campos en un solo índice,
            // por eso filtramos lon/fecha/texto en memoria luego de traer un bounding por lat.
            .limit(200)

        val snap = q.get().await()
        val raw = snap.toObjects(Flyer::class.java)

        return raw.filter { flyer ->
            val loc = flyer.location
            val insideLon = loc.longitude in sw.lng..ne.lng
            val insideDate = when {
                fromDateMillis != null && toDateMillis != null -> {
                    val d = flyer.dateMillis ?: flyer.timestamp
                    d in fromDateMillis..toDateMillis
                }
                fromDateMillis != null -> (flyer.dateMillis ?: flyer.timestamp) >= fromDateMillis
                toDateMillis != null -> (flyer.dateMillis ?: flyer.timestamp) <= toDateMillis
                else -> true
            }
            val promotedOk = if (promotedOnly) (flyer.adOption == "PROMOTED" || flyer.adOption == "HIGHLIGHTED") else true
            val textOk = text.isNullOrBlank() || listOf(
                flyer.title, flyer.description, flyer.city, flyer.highlightedText
            ).any { it?.contains(text!!, ignoreCase = true) == true }

            val centerOk = haversineKm(center, LatLng(loc.latitude, loc.longitude)) <= radiusKm

            insideLon && insideDate && promotedOk && textOk && centerOk
        }
            .sortedByDescending { it.dateMillis ?: it.timestamp }
    }
}
