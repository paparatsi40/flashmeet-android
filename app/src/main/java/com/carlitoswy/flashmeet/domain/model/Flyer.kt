package com.carlitoswy.flashmeet.domain.model

import com.google.firebase.firestore.GeoPoint

data class Flyer(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdBy: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val bgColor: Int = 0xFFFFFFFF.toInt(),
    val fontName: String = "Sans",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val adOption: String = "NONE",
    val highlightedText: String = "",
    val distanceKm: Double? = null // ➕ distancia calculada opcional
)
