package com.carlitoswy.flashmeet.utils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

suspend fun CameraPositionState.centerOnLocation(lat: Double, lon: Double, zoom: Float = 14f) {
    animate(update = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom), durationMs = 1000)
}
