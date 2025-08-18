package com.carlitoswy.flashmeet.domain.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val adOption: String = "NONE",
    val highlightedText: String = "",
    val createdBy: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val locationName: String = "",
    val category: EventCategory = EventCategory.OTHER, // 🔥 Ahora es enum
    val imageUrl: String? = null,

    // ✅ Campos para ubicación
    val country: String = "",
    val city: String = "",
    val postal: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

// ✅ Conversión para Firestore
fun EventCategory.toFirestoreString(): String = this.name

fun String?.toEventCategory(): EventCategory =
    EventCategory.values().find { it.name.equals(this, ignoreCase = true) } ?: EventCategory.OTHER
