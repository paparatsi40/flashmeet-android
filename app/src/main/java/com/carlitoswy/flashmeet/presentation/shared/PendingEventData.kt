package com.carlitoswy.flashmeet.presentation.shared

import com.carlitoswy.flashmeet.domain.model.AdOption

/**
 * Estado temporal del evento antes de persistirlo/confirmar pago.
 * - id: puede ser null si aún no has creado el evento en backend.
 */
data class PendingEventData(
    val id: String? = null,        // 👈 ahora es opcional y con valor por defecto
    val title: String,
    val description: String,
    val imageUri: String?,
    val locationName: String,
    val userId: String,
    val adOption: AdOption,
    val highlightedText: String
)
