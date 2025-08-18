package com.carlitoswy.flashmeet.presentation.shared

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.carlitoswy.flashmeet.domain.model.AdOption

@Composable
fun AdOptionChip(
    option: AdOption,
    selected: Boolean, // Este parámetro es perfecto para FilterChip
    onSelect: () -> Unit
) {
    FilterChip(
        // FilterChip sí tiene un parámetro 'selected'
        selected = selected,
        onClick = onSelect,
        label = { Text(option.displayName) },
        // FilterChipDefaults.filterChipColors() puede proveer los colores
        // por defecto para estados seleccionados y no seleccionados automáticamente.
        // Si quieres colores personalizados cuando está seleccionado, puedes configurarlos aquí:
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary, // Fondo cuando está seleccionado
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary // Color del texto cuando está seleccionado
            // Puedes añadir selectedLeadingIconColor, selectedTrailingIconColor, etc.
        )
    )
}
