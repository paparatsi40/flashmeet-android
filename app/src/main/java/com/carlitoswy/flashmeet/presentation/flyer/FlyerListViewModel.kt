package com.carlitoswy.flashmeet.presentation.flyer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.data.location.LocationService
import com.carlitoswy.flashmeet.domain.model.Flyer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class FlyerListViewModel @Inject constructor(
    application: Application,
    private val locationService: LocationService
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()

    private val _flyers = MutableStateFlow<List<Flyer>>(emptyList())
    val flyers: StateFlow<List<Flyer>> = _flyers

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentLocation: GeoPoint? = null

    var radiusKm = 5.0

    init {
        fetchLocationAndLoad()
    }

    fun updateRadius(newRadius: Double) {
        radiusKm = newRadius
        loadFlyers()
    }

    private fun fetchLocationAndLoad() {
        viewModelScope.launch {
            locationService.getLastKnownLocation()?.let { loc ->
                currentLocation = GeoPoint(loc.latitude, loc.longitude)
            }
            loadFlyers()
        }
    }

    private fun loadFlyers() {
        viewModelScope.launch {
            try {
                firestore.collection("flyers")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            _error.value = "Error al cargar flyers: ${e.message}"
                            return@addSnapshotListener
                        }
                        snapshot?.let {
                            val flyers = it.documents.mapNotNull { doc ->
                                doc.toObject(Flyer::class.java)?.copy(id = doc.id)
                            }.filter { flyer ->
                                currentLocation?.let { curr ->
                                    flyer.location?.let { flyerLoc ->
                                        distanceBetween(curr, flyerLoc) <= radiusKm
                                    } ?: false
                                } ?: true
                            }
                            _flyers.value = flyers
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    private fun distanceBetween(a: GeoPoint, b: GeoPoint): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)

        val aVal = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(aVal), sqrt(1 - aVal))

        return earthRadius * c // en kilómetros
    }
}