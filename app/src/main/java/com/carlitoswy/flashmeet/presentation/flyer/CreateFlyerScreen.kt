package com.carlitoswy.flashmeet.presentation.flyer

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFlyerScreen(
    navController: NavHostController,
    viewModel: CreateFlyerViewModel = hiltViewModel(),
    onFlyerCreated: () -> Unit
) {
    // Estado local de los campos
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bgColor by remember { mutableStateOf(Color.White) }
    var font by remember { mutableStateOf(FontFamily.SansSerif) }
    var adOption by remember { mutableStateOf(AdOption.NONE) }
    var highlightedText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CreditCard") }

    // ➕ NUEVO: ciudad para locationLabel
    var city by remember { mutableStateOf("") }

    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val estimatedCost = viewModel.estimateAdCost(adOption)

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        // (Opcional) Si quieres reflejar que hay imagen pendiente:
        viewModel.updateDraft(mapOf("imageUrl" to "")) // o sube primero y luego guarda URL real
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) navController.navigate("camera_capture")
        else Log.e("CreateFlyer", "PERM denied")
    }

    // ➕ NUEVO: crear borrador al entrar
    LaunchedEffect(Unit) {
        // TODO: sustituir "user123" por el userId real (FirebaseAuth.currentUser?.uid)
        viewModel.beginDraft(createdBy = "user123", locationLabel = city)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Flyer") },
                actions = {
                    TextButton(onClick = {
                        if (adOption == AdOption.NONE) {
                            // Publicar sin pago: reutiliza draftId
                            viewModel.createFlyer(
                                title = title,
                                description = description,
                                imageUri = imageUri,
                                bgColor = bgColor.toArgb(),
                                fontName = when (font) {
                                    FontFamily.Serif -> "Serif"
                                    FontFamily.Monospace -> "Mono"
                                    else -> "Sans"
                                },
                                locationLabel = city,        // ⬅️ usa ciudad real
                                createdBy = "user123",       // ⬅️ reemplaza con uid real
                                adOption = adOption,
                                highlightedText = highlightedText,
                                onSuccess = onFlyerCreated
                            )
                        } else {
                            // Guardar pendiente y navegar a Pago
                            viewModel.savePendingFlyer(
                                title = title,
                                description = description,
                                imageUri = imageUri,
                                bgColor = bgColor.toArgb(),
                                fontName = when (font) {
                                    FontFamily.Serif -> "Serif"
                                    FontFamily.Monospace -> "Mono"
                                    else -> "Sans"
                                },
                                locationLabel = city,        // ⬅️ usa ciudad real
                                createdBy = "user123",       // ⬅️ reemplaza con uid real
                                adOption = adOption,
                                highlightedText = highlightedText
                            )
                            navController.navigate(Routes.PAYMENT)
                        }
                    }) { Text("Guardar") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlyerPreview(
                title = title,
                description = description,
                imageUri = imageUri,
                bgColor = bgColor,
                font = font,
                highlightedText = highlightedText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "cam")
                }
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = "gal")
                }
            }

            // Título (actualiza borrador)
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    viewModel.updateDraft(mapOf("title" to it))
                },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            // Descripción (actualiza borrador)
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    viewModel.updateDraft(mapOf("description" to it))
                },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            // Ciudad (nuevo campo en UI + borrador)
            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    viewModel.updateDraft(mapOf("city" to it))
                },
                label = { Text("Ciudad") },
                modifier = Modifier.fillMaxWidth()
            )

            // Texto resaltado (actualiza borrador)
            OutlinedTextField(
                value = highlightedText,
                onValueChange = {
                    highlightedText = it
                    viewModel.updateDraft(mapOf("highlightedText" to it))
                },
                label = { Text("Texto Resaltado") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Color de fondo:")
            LazyRow {
                items(listOf(Color.White, Color.Yellow, Color.Cyan, Color.LightGray)) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color)
                            .clickable {
                                bgColor = color
                                viewModel.updateDraft(mapOf("bgColor" to color.toArgb()))
                            }
                    )
                }
            }

            Text("Fuente:")
            LazyRow {
                items(
                    listOf(
                        FontFamily.SansSerif to "Sans",
                        FontFamily.Serif to "Serif",
                        FontFamily.Monospace to "Mono"
                    )
                ) { (ff, label) ->
                    Text(
                        label,
                        fontFamily = ff,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                font = ff
                                val fontName = when (ff) {
                                    FontFamily.Serif -> "Serif"
                                    FontFamily.Monospace -> "Mono"
                                    else -> "Sans"
                                }
                                viewModel.updateDraft(mapOf("fontName" to fontName))
                            }
                    )
                }
            }

            Text("Publicidad:")
            LazyRow {
                items(AdOption.entries) { option ->
                    AdOptionChip(option, option == adOption) {
                        adOption = option
                        viewModel.updateDraft(mapOf("adOption" to option.name))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(12.dp)
            ) {
                Text("Costo estimado: $estimatedCost", color = MaterialTheme.colorScheme.onSecondary)
            }

            OutlinedButton(
                onClick = { /* Lógica para elegir método de pago */ },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Método de Pago Placeholder")
            }

            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun FlyerPreview(
    title: String,
    description: String,
    imageUri: Uri?,
    bgColor: Color,
    font: FontFamily,
    highlightedText: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(bgColor), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Flyer",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_placeholder),
                    contentDescription = "Placeholder",
                    modifier = Modifier.size(80.dp)
                )
            }
            Text(title, fontFamily = font, style = MaterialTheme.typography.headlineSmall)
            Text(description, fontFamily = font)
            if (highlightedText.isNotEmpty()) {
                Text(highlightedText, fontFamily = font, color = Color.Red)
            }
        }
    }
}

@Composable
fun AdOptionChip(option: AdOption, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(option.name) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
            labelColor = if (selected) Color.White else Color.Black
        ),
        modifier = Modifier.padding(end = 8.dp)
    )
}
