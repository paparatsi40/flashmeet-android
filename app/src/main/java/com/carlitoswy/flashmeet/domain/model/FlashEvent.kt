package com.carlitoswy.flashmeet.domain.model

data class FlashEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val time: Long = 0L,
    val createdBy: String = "",
    val imageUrl: String = "",
    val participants: List<String> = emptyList(),
    val adOption: String = "" // <-- ADD THIS LINE (provide a default value if appropriate)
)