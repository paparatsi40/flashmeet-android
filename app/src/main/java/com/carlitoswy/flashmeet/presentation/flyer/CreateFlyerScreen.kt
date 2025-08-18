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
import androidx.compose.foundation.lazy.items // Necesario si usas items(List)
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
import androidx.compose.material3.OutlinedButton // Asegúrate de tener esta importación si la usas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource // Necesario si usas painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.carlitoswy.flashmeet.R // Asegúrate de tener esta importación para R.drawable
import com.carlitoswy.flashmeet.domain.model.AdOption
// Las siguientes importaciones ya NO son necesarias aquí, las hemos movido al ViewModel:
// import com.carlitoswy.flashmeet.presentation.shared.PendingFlyerData
// import com.carlitoswy.flashmeet.presentation.shared.SharedFlyerStateViewModel
import com.carlitoswy.flashmeet.ui.navigation.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFlyerScreen(
    navController: NavHostController,
    viewModel: CreateFlyerViewModel = hiltViewModel(), // <-- Ahora este es el ViewModel principal
    onFlyerCreated: () -> Unit
) {
    // ELIMINADO: Ya no se inyecta SharedFlyerStateViewModel aquí
    // sharedFlyerState: SharedFlyerStateViewModel = hiltViewModel(),

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bgColor by remember { mutableStateOf(Color.White) }
    var font by remember { mutableStateOf(FontFamily.SansSerif) }
    var adOption by remember { mutableStateOf(AdOption.NONE) }
    var highlightedText by remember { mutableStateOf("") }
    // Asumiendo que paymentMethod aún se usa y no causa problemas con el error de arriba
    var paymentMethod by remember { mutableStateOf("CreditCard") }


    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val estimatedCost = viewModel.estimateAdCost(adOption)

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) navController.navigate("camera_capture")
        else Log.e("CreateFlyer", "PERM denied")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Flyer") },
                actions = {
                    TextButton(onClick = {
                        if (adOption == AdOption.NONE) {
                            // Si no hay opción de anuncio, crear flyer directamente
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
                                locationLabel = "Ubicación Actual",
                                createdBy = "user123",
                                adOption = adOption,
                                highlightedText = highlightedText,
                                onSuccess = onFlyerCreated
                            )
                        } else {
                            // Si hay opción de anuncio, guardar como pendiente y navegar a pago
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
                                locationLabel = "Ubicación Actual",
                                createdBy = "user123",
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

            OutlinedTextField(title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(highlightedText, onValueChange = { highlightedText = it }, label = { Text("Texto Resaltado") }, modifier = Modifier.fillMaxWidth())

            Text("Color de fondo:")
            LazyRow { items(listOf(Color.White, Color.Yellow, Color.Cyan, Color.LightGray)) { color ->
                Box(modifier = Modifier.size(40.dp).background(color).clickable { bgColor = color })
            }}

            Text("Fuente:")
            LazyRow { items(listOf(FontFamily.SansSerif to "Sans", FontFamily.Serif to "Serif", FontFamily.Monospace to "Mono")) { (ff, label) ->
                Text(label, fontFamily = ff, modifier = Modifier.padding(8.dp).clickable { font = ff })
            }}

            Text("Publicidad:")
            LazyRow { items(AdOption.entries) { option ->
                AdOptionChip(option, option == adOption) { adOption = option }
            }}

            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.small) // Añadí shape para consistencia con tu código original
                .padding(12.dp)) {
                Text("Costo estimado: $estimatedCost", color = MaterialTheme.colorScheme.onSecondary)
            }

            // Aquí estaba el Row para paymentMethod, lo reinserto si se quitó accidentalmente
            OutlinedButton( // Usando OutlinedButton directamente
                onClick = { /* Lógica para PaymentMethod */ },
                //colors = ButtonDefaults.outlinedButtonColors( // Comentado porque ButtonDefaults requiere importación específica
                //    containerColor = if (paymentMethod == "CreditCard") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                //),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Método de Pago Placeholder") // Placeholder Text
            }


            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.weight(1f))
        }
    }
}

// Estas funciones deben estar en el mismo CreateFlyerScreen.kt:

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
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Flyer",
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            } else {
                // Usar el recurso R.drawable.ic_placeholder que tenías originalmente
                Image(
                    painter = painterResource(R.drawable.ic_placeholder),
                    contentDescription = "Placeholder",
                    modifier = Modifier.size(80.dp)
                )
            }
            Text(title, fontFamily = font, style = MaterialTheme.typography.headlineSmall)
            Text(description, fontFamily = font)
            if (highlightedText.isNotEmpty()) Text(highlightedText, fontFamily = font, color = Color.Red)
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
