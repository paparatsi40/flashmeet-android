package com.carlitoswy.flashmeet.presentation.event

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.presentation.components.ColorPickerDialog
import com.carlitoswy.flashmeet.presentation.components.FontPickerDialog
import com.carlitoswy.flashmeet.ui.theme.toHex
import kotlinx.coroutines.delay

@Composable
fun CreateEventScreen(
    navController: NavHostController,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    var showColorDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }

    // Lottie animation setup
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("confetti_success.json"))
    val lottieAnimState = rememberLottieAnimatable()

    LaunchedEffect(state.success) {
        if (state.success) {
            lottieAnimState.animate(
                composition = composition,
                iterations = 1
            )
            delay(2500)
            navController.popBackStack() // O redirige a donde quieras
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.success) {
            // Confetti animation
            LottieAnimation(
                composition = composition,
                progress = lottieAnimState.progress,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    label = { Text("Título del evento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.onDescriptionChanged(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.city,
                    onValueChange = { viewModel.onCityChanged(it) },
                    label = { Text("Ciudad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.date,
                    onValueChange = { viewModel.onDateChanged(it) },
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { showColorDialog = true }) {
                        Text("Color de fondo")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = Color((state.flyerBackgroundColor ?: "#FFFFFF").toColorInt()),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { showFontDialog = true }) {
                        Text("Fuente")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = state.flyerFontFamily ?: "Por defecto")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Promoción", style = MaterialTheme.typography.titleMedium)
                    AdOption.entries.forEach { option ->
                        val selected = state.adOption == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onAdOptionSelected(option) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    option.displayName,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                                Text(option.description, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                option.displayPrice(),
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.onCreateEvent(context) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Crear evento")
                }
            }

            if (showColorDialog) {
                ColorPickerDialog(
                    title = "Color de fondo",
                    selectedColorHex = state.flyerBackgroundColor,
                    onColorSelected = {
                        viewModel.onFlyerBackgroundColorChange(it.toHex())
                        showColorDialog = false
                    },
                    onDismiss = { showColorDialog = false }
                )
            }

            if (showFontDialog) {
                state.flyerFontFamily?.let {
                    FontPickerDialog(
                        selectedFont = it,
                        onFontSelected = {
                            viewModel.onFontFamilyChanged(it)
                            showFontDialog = false
                        },
                        onDismiss = { showFontDialog = false }
                    )
                }
            }
        }
    }
}
