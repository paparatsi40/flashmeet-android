package com.carlitoswy.flashmeet.ui.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.carlitoswy.flashmeet.presentation.flyer.FlyerEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlyerEditorScreen(
    navController: NavHostController,
    viewModel: FlyerEditorViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val bgColor by viewModel.bgColor.collectAsState()
    val fontName by viewModel.fontName.collectAsState()
    val status by viewModel.status.collectAsState()

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.setImage(it) } }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            navController.navigate("camera_capture")
        } else {
            Log.e("FlyerEditor", "Permiso de cámara denegado")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Flyer") },
                actions = {
                    TextButton(onClick = { viewModel.saveFlyer("demoUser") }) {
                        Text("Guardar")
                    }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(bgColor)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = "Flyer image")
                } else {
                    Text("Toca un botón para agregar imagen")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto")
                }

                IconButton(onClick = {
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.Image, contentDescription = "Elegir imagen")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Color de fondo:")
            LazyRow {
                val colors = listOf(Color.White, Color.Yellow, Color.Cyan, Color.LightGray)
                items(colors.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors[i])
                            .clickable { viewModel.bgColor.value = colors[i].value.toInt() }
                    )
                }
            }

            Text("Fuente:")
            LazyRow {
                val fonts = listOf(
                    FontFamily.SansSerif to "Sans",
                    FontFamily.Serif to "Serif",
                    FontFamily.Monospace to "Mono"
                )
                items(fonts.size) { i ->
                    Text(
                        text = fonts[i].second,
                        fontFamily = fonts[i].first,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { viewModel.fontName.value = fonts[i].second }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // Navegar al terminar
    LaunchedEffect(status) {
        if (status == "ok") {
            navController.popBackStack()
        }
    }
}
