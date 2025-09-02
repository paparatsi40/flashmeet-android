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
    val category: EventCategory? = null,
    val imageUrl: String? = null,
    val interestedCount: Int? = 0,

    // 🌍 Ubicación geográfica (GPS + info textual)
    val country: String = "",
    val city: String = "",
    val postal: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // 🎨 Estilo del flyer (opcional)
    val flyerTextColor: String? = null,        // Ej: "#FFFFFF"
    val flyerBackgroundColor: String? = null,  // Ej: "#000000"
    val flyerFontFamily: String? = null        // Ej: "Cursive", "Roboto"
)

// 🔁 Conversión para Firestore
fun EventCategory.toFirestoreString(): String = this.name

fun String?.toEventCategory(): EventCategory =
    EventCategory.entries.find { it.name.equals(this, ignoreCase = true) } ?: EventCategory.OTHER
