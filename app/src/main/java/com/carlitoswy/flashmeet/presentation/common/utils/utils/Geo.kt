package com.carlitoswy.flashmeet.utils

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLng(val lat: Double, val lng: Double)

fun haversineKm(a: LatLng, b: LatLng): Double {
    val R = 6371.0
    val dLat = Math.toRadians(b.lat - a.lat)
    val dLon = Math.toRadians(b.lng - a.lng)
    val lat1 = Math.toRadians(a.lat)
    val lat2 = Math.toRadians(b.lat)
    val h = sin(dLat/2).pow(2.0) + sin(dLon/2).pow(2.0) * cos(lat1) * cos(lat2)
    return 2 * R * asin(sqrt(h))
}

fun boundingBox(center: LatLng, radiusKm: Double): Pair<LatLng, LatLng> {
    val latDelta = Math.toDegrees(radiusKm / 6371.0)
    val lonDelta = Math.toDegrees(radiusKm / (6371.0 * cos(Math.toRadians(center.lat))))
    val sw = LatLng(center.lat - latDelta, center.lng - lonDelta)
    val ne = LatLng(center.lat + latDelta, center.lng + lonDelta)
    return sw to ne
}
