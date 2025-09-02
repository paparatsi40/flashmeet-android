package com.carlitoswy.flashmeet.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.carlitoswy.flashmeet.domain.model.Event
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailBottomSheet(
    event: Event,
    bottomSheetState: SheetState,
    onDismiss: () -> Unit,
    // Opcionales (integración con “Me interesa”)
    interested: Boolean = false,
    onToggleInterest: (() -> Unit)? = null,
    // 👇 NUEVO: etiqueta de distancia formateada "x.x km"
    distanceLabel: String? = null
) {
    val context = LocalContext.current
    var isInterested by remember(interested) { mutableStateOf(interested) }

    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (event.adOption.equals("HIGHLIGHTED", true)) "⭐ ${event.title}" else event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            // Imagen
            if (!event.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                    AsyncImage(
                        model = event.imageUrl,
                        contentDescription = event.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Fecha
            Text(
                text = formatEventDate(event.timestamp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Ubicación + Distancia
            if (event.locationName.isNotBlank() || distanceLabel != null) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (event.locationName.isNotBlank()) {
                        Text(
                            text = "📍 ${event.locationName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (distanceLabel != null) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = { /* no-op */ },
                            enabled = false,
                            label = { Text(distanceLabel) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Descripción
            if (event.description.isNotBlank()) {
                Text(event.description, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(16.dp))

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { openDirections(context, event.latitude, event.longitude, event.title) }
                ) {
                    Text("Cómo llegar")
                }
                Spacer(Modifier.width(12.dp))
                TextButton(onClick = { shareEvent(context, event) }) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Compartir")
                }
                Spacer(Modifier.weight(1f))
                if (onToggleInterest != null) {
                    IconToggleButton(
                        checked = isInterested,
                        onCheckedChange = {
                            isInterested = !isInterested
                            onToggleInterest()
                        }
                    ) {
                        if (isInterested) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "Quitar de interesados",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Me interesa")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ------------------- Helpers internos ------------------- */

private fun formatEventDate(ts: Long): String {
    val now = Instant.now()
    val inst = Instant.ofEpochMilli(ts)
    val days = ChronoUnit.DAYS.between(
        now.truncatedTo(ChronoUnit.DAYS),
        inst.truncatedTo(ChronoUnit.DAYS)
    )
    val dateStr = DateTimeFormatter.ofPattern("EEE d MMM • HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(inst)

    return when {
        days == 0L -> "Hoy • $dateStr"
        days == 1L -> "Mañana • $dateStr"
        days in 2..6 -> "Esta semana • $dateStr"
        days < 0 -> "Pasado • $dateStr"
        else -> dateStr
    }
}

private fun shareEvent(context: Context, event: Event) {
    val text = buildString {
        appendLine("📅 ${event.title}")
        if (event.locationName.isNotBlank()) appendLine("📍 ${event.locationName}")
        appendLine(formatEventDate(event.timestamp))
        if (event.description.isNotBlank()) {
            appendLine()
            appendLine(event.description)
        }
        // Si tienes un deeplink, agrégalo aquí:
        // appendLine("\nVer en FlashMeet: https://flashmeet.app/event/${event.id}")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Compartir evento"))
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo compartir", Toast.LENGTH_SHORT).show()
    }
}

private fun openDirections(context: Context, lat: Double, lon: Double, label: String = "Evento") {
    val gmm = Uri.parse("google.navigation:q=$lat,$lon&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmm).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapIntent)
    } catch (_: ActivityNotFoundException) {
        val uri = Uri.parse("https://maps.google.com/?q=$lat,$lon($label)")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir mapas", Toast.LENGTH_SHORT).show()
        }
    }
}
