package com.carlitoswy.flashmeet.presentation.event

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.color
import com.carlitoswy.flashmeet.domain.model.icon
import com.carlitoswy.flashmeet.domain.model.toCategoryEnum
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.carlitoswy.flashmeet.utils.dateFormatted
import com.carlitoswy.flashmeet.utils.navigateWithGoogleMaps
import com.carlitoswy.flashmeet.utils.shareEvent

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventDetailViewModel = hiltViewModel() // <-- Cambiado a EventDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState() // <-- Recoge el uiState unificado
    val context = LocalContext.current

    LaunchedEffect(eventId) { viewModel.loadEventById(eventId) } // <-- Llama a loadEventById

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                actions = {
                    // Asegúrate de que event no sea null antes de compartir
                    IconButton(onClick = { uiState.event?.let { shareEvent(context, it) } }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                }
            )
        }
    ) { paddingValues -> // Usamos paddingValues para el content
        // Usamos Box con Modifier.padding(paddingValues) para aplicar el padding del Scaffold
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.errorMessage}")
                }
                uiState.event != null -> EventDetailContent(uiState.event!!, navController, context)
            }
        }
    }
}

@Composable
private fun EventDetailContent(event: Event, navController: NavController, context: Context) {
    val categoryEnum = event.toCategoryEnum()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 🖼 Imagen si existe
        event.imageUrl?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = event.title,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(12.dp))

        // 🏷️ Título y categoría (ajustado para que el título del evento sea el principal)
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineSmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            categoryEnum?.let {
                Icon(
                    imageVector = it.icon(),
                    contentDescription = it.displayName(), // Usar displayName si está disponible
                    tint = it.color(),
                    modifier = Modifier.size(20.dp).padding(end = 4.dp) // Ajuste de tamaño
                )
            }
            // Mueve la fecha formateada aquí para que esté junto a la categoría o debajo del título
            Text(
                text = event.timestamp.dateFormatted(), // <-- ESTA LÍNEA AHORA ES CORRECTA
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }


        // 🌍 Ubicación
        if (event.city.isNotEmpty() || event.country.isNotEmpty()) {
            Text(
                text = "${event.city}, ${event.country}".trim().trim(','),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 📝 Descripción
        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 🔘 Acciones
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { shareEvent(context, event) }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
                Spacer(Modifier.width(6.dp))
                Text("Compartir")
            }
            OutlinedButton(onClick = { navigateWithGoogleMaps(context, event) }) {
                Icon(Icons.Default.MyLocation, contentDescription = "Navegar")
                Spacer(Modifier.width(6.dp))
                Text("Navegar")
            }
            OutlinedButton(onClick = { navController.navigate(Routes.editEventWithId(event.id)) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
                Spacer(Modifier.width(6.dp))
                Text("Editar")
            }
        }
    }
}
