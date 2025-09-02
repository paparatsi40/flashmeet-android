package com.carlitoswy.flashmeet.domain.model

import com.google.firebase.firestore.GeoPoint

data class Flyer(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val createdBy: String,
    val timestamp: Long,
    val location: GeoPoint,
    val bgColor: Int,
    val fontName: String,
    val adOption: String,
    val highlightedText: String,
    // existentes recientes
    val eventId: String? = null,
    val priceCents: Long? = null,
    // ⬇️ NUEVOS (opcionales para no romper nada)
    val ownerId: String? = null,
    val dateMillis: Long? = null,
    val city: String? = null // si ya manejas ciudad, te servirá para filtros
)
