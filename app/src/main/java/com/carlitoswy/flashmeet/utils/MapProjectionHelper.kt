package com.carlitoswy.flashmeet.utils

import androidx.compose.ui.unit.IntOffset
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.projection.SphericalMercatorProjection

/**
 * Convierte un LatLng en coordenadas relativas dentro de la pantalla del mapa.
 * Nota: Este es un cálculo aproximado porque Google Maps Compose no expone projection.toScreenLocation.
 */
fun LatLng.toScreenOffset(camera: CameraPositionState, mapWidth: Int, mapHeight: Int): IntOffset {
    val zoom = camera.position.zoom
    val scale = 1 shl zoom.toInt()
    val projection = SphericalMercatorProjection(256.0 * scale)

    val mapCenter = projection.toPoint(camera.position.target)
    val point = projection.toPoint(this)

    val dx = (point.x - mapCenter.x).toInt() + mapWidth / 2
    val dy = (point.y - mapCenter.y).toInt() + mapHeight / 2

    return IntOffset(dx, dy)
}
