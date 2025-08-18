package com.carlitoswy.flashmeet.presentation.event

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.presentation.shared.AdOptionChip
import com.carlitoswy.flashmeet.presentation.shared.PendingEventData
import com.carlitoswy.flashmeet.presentation.shared.SharedEventStateViewModel
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavHostController,
    createEventViewModel: CreateEventViewModel = hiltViewModel(),
    sharedEventStateViewModel: SharedEventStateViewModel = hiltViewModel(),
    onEventCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var highlightedText by remember { mutableStateOf("") }
    var adOption by remember { mutableStateOf(AdOption.NONE) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var locationName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    val loading by createEventViewModel.isLoading.collectAsState()
    val error by createEventViewModel.errorMessage.collectAsState()
    val estimatedCost = createEventViewModel.estimateAdCost(adOption)

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        // TODO: Guardar como archivo y convertir a Uri si lo necesitas
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Crear Evento") },
            actions = {
                TextButton(onClick = {
                    if (adOption == AdOption.NONE) {
                        createEventViewModel.createEvent(
                            title = title,
                            description = description,
                            adOption = adOption,
                            highlightedText = highlightedText,
                            imageUri = imageUri,
                            locationName = locationName,
                            onSuccess = onEventCreated
                        )
                    } else {
                        sharedEventStateViewModel.savePendingEvent(
                            PendingEventData(
                                title = title,
                                description = description,
                                imageUri = imageUri?.toString(),
                                locationName = locationName,
                                userId = userId,
                                adOption = adOption,
                                highlightedText = highlightedText
                            )
                        )
                        navController.navigate(Routes.PAYMENT)
                    }
                }) {
                    Text("Guardar")
                }
            }
        )
    }) { padding ->
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
                label = { Text("Ubicación del evento") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { cameraLauncher.launch(null) }) { Text("Cámara") }
                Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Galería") }
            }

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            Text("Publicidad:")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AdOption.entries) { option ->
                    AdOptionChip(option, selected = adOption == option) {
                        adOption = option
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.small)
                    .padding(12.dp)
            ) {
                Text(
                    "Costo estimado: $estimatedCost",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
