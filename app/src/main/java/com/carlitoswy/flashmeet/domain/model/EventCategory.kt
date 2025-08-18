package com.carlitoswy.flashmeet.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ✅ Categorías disponibles para los eventos en FlashMeet.
 * Incluye nombre visible, emoji, icono y color asociados.
 */
enum class EventCategory(
    val label: String,   // Nombre visible
    val emoji: String,   // Emoji representativo
    val icon: ImageVector,
    val color: Color
) {
    MUSIC("Música", "🎵", Icons.Default.MusicNote, Color(0xFF2196F3)),
    BUSINESS("Negocios", "💼", Icons.Default.Work, Color(0xFF9C27B0)),
    SOCIAL("Social", "🎉", Icons.Default.SportsSoccer, Color(0xFFF44336)),
    TECH("Tecnología", "💻", Icons.Default.Work, Color(0xFFFF9800)),
    OTHER("Otro", "🌀", Icons.Default.Event, Color.Gray);

    /**
     * Texto amigable para mostrar en UI (emoji + label).
     */
    fun displayName(): String = "$emoji $label"

    companion object {
        /**
         * Convierte un string a su categoría correspondiente.
         */
        fun fromString(value: String?): EventCategory? =
            entries.find { it.name.equals(value, true) || it.label.equals(value, true) }
    }
}
