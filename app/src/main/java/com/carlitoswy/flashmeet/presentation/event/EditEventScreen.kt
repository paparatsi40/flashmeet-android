package com.carlitoswy.flashmeet.presentation.event

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.presentation.shared.AdOptionChip
import kotlinx.coroutines.launch

// Si no están cubiertos por material3.* o runtime.*, podrías necesitar estos explícitamente:
// import androidx.compose.material3.SnackbarHost
// import androidx.compose.material3.SnackbarHostState
// import androidx.compose.material3.SnackbarResult // Aunque no lo usas directamente, showSnackbar lo retorna.
// import androidx.compose.material3.rememberSnackbarHostState // Si prefieres esta en lugar de remember { SnackbarHostState() }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavHostController,
    eventId: String,
    viewModel: EditEventViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() } // Ya lo tenías, equivalente a rememberSnackbarHostState() en Material3

    val uiState by viewModel.uiState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    // Estados para los campos del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var highlightedText by remember { mutableStateOf("") }
    var adOption by remember { mutableStateOf(AdOption.NONE) }
    var locationName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Cargar el evento al iniciar
    LaunchedEffect(eventId) {
        val currentEventInState = uiState.event
        if (currentEventInState == null || currentEventInState.id != eventId) {
            viewModel.loadEventById(eventId)
        }
    }

    // Actualizar estados cuando el evento esté disponible
    LaunchedEffect(uiState.event) {
        uiState.event?.let { event ->
            title = event.title
            description = event.description
            highlightedText = event.highlightedText ?: ""
            locationName = event.locationName
            adOption = AdOption.valueOf(event.adOption)
            event.imageUrl?.let { imageUrl ->
                imageUri = Uri.parse(imageUrl)
            }
        }
    }

    // Muestra el Snackbar cuando haya error (Ya implementado)
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            if (errorMessage.isNotBlank()) {
                snackbarHostState.showSnackbar(errorMessage)
                // Opcional: Limpiar el mensaje de error en el ViewModel después de mostrarlo
                // viewModel.clearErrorMessage()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Ya lo tenías, usando el snackbarHostState creado
        topBar = {
            TopAppBar(
                title = { Text("Editar Evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        uiState.event?.let { currentEvent ->
                            coroutineScope.launch {
                                // Validación básica
                                if (title.isBlank() || description.isBlank()) {
                                    snackbarHostState.showSnackbar("Completa los campos de Título y Descripción.")
                                    return@launch
                                }

                                viewModel.updateEvent(
                                    eventId = currentEvent.id,
                                    title = title,
                                    description = description,
                                    adOption = adOption.name,
                                    highlightedText = highlightedText,
                                    imageUri = imageUri,
                                    locationName = locationName,
                                    onSuccess = {
                                        // Muestra un Snackbar de éxito al guardar (emoji removido)
                                        snackbarHostState.showSnackbar("Evento actualizado correctamente")
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = highlightedText,
                onValueChange = { highlightedText = it },
                label = { Text("Texto resaltado") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Imagen del evento (toque para cambiar)", style = MaterialTheme.typography.labelLarge)

            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Imagen seleccionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )
            } else {
                Text(
                    text = "Toca aquí para seleccionar imagen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(16.dp),
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Text("Publicidad:")
            LazyRow {
                items(AdOption.entries) { option ->
                    AdOptionChip(
                        option = option,
                        selected = option == adOption
                    ) {
                        adOption = option
                    }
                }
            }

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
