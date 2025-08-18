package com.carlitoswy.flashmeet.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlitoswy.flashmeet.domain.model.EventCategory

@Composable
fun EventCategoryFilterChips(
    selectedCategory: EventCategory?,
    onCategorySelected: (EventCategory?) -> Unit,
    showOnlyHighlighted: Boolean,
    onHighlightedToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        // 🔁 Chips por categoría
        EventCategory.entries.forEach { category ->
            CategoryChip(
                category = category,
                isSelected = selectedCategory == category,
                onClick = {
                    onCategorySelected(if (selectedCategory == category) null else category)
                }
            )
        }

        // ⭐ Chip para Destacados
        AssistChip(
            onClick = { onHighlightedToggle(!showOnlyHighlighted) },
            label = { Text("⭐ Destacados") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (showOnlyHighlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                labelColor = if (showOnlyHighlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
