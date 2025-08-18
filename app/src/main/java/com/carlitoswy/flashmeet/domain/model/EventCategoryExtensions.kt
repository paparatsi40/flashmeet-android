package com.carlitoswy.flashmeet.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ✅ Extensiones para obtener icono y color asociados a cada categoría.
 */
fun EventCategory.icon(): ImageVector = when (this) {
    EventCategory.MUSIC -> Icons.Default.MusicNote
    EventCategory.BUSINESS -> Icons.Default.Work
    EventCategory.SOCIAL -> Icons.Default.SportsSoccer
    EventCategory.TECH -> Icons.Default.Work
    EventCategory.OTHER -> Icons.Default.Event
}

fun EventCategory.color(): Color = when (this) {
    EventCategory.MUSIC -> Color(0xFF2196F3)    // Azul
    EventCategory.BUSINESS -> Color(0xFF9C27B0) // Púrpura
    EventCategory.SOCIAL -> Color(0xFFF44336)   // Rojo
    EventCategory.TECH -> Color(0xFFFF9800)     // Naranja
    EventCategory.OTHER -> Color.Gray           // Gris por defecto
}
