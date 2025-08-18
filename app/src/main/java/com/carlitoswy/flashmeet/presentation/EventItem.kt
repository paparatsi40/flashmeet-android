package com.carlitoswy.flashmeet.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.text.color
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.EventCategory
import com.carlitoswy.flashmeet.utils.dateFormatted

@Composable
fun EventItem(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryEnum = EventCategory.fromString(event.category.toString())

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categoryEnum?.let {
                categoryEnum?.let { category -> // Renombré 'it' a 'category' para mayor claridad
                    Icon(
                        imageVector = category.icon,  // Accede a la propiedad icon
                        contentDescription = category.displayName(), // displayName() sigue siendo una función
                        tint = category.color,       // Accede a la propiedad color
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }


                Column {
                    Text(event.title, style = MaterialTheme.typography.titleMedium)

                    if (event.city.isNotEmpty()) {
                        Text(
                            text = "${event.city}, ${event.country}".trim().trim(','),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    // Formatea la fecha a partir del timestamp usando la extensión
                    Text(
                        text = event.timestamp.dateFormatted(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
