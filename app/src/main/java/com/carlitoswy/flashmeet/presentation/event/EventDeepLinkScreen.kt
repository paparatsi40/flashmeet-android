package com.carlitoswy.flashmeet.presentation.event

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.ui.navigation.Routes

/**
 * Pantalla “intermedia” para manejar el deep link:
 * 1) Carga el evento por id.
 * 2) Si existe, navega al Home con foco en ese evento (y centrado).
 * 3) Si hay error, cierra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDeepLinkScreen(
    eventId: String,
    onClose: () -> Unit,
    navController: NavController,
    vm: EventDetailViewModel = hiltViewModel() // <-- CAMBIO 1: Nombre del ViewModel
) {
    // CAMBIO 2: Observa el uiState unificado
    val uiState by vm.uiState.collectAsState()

    // CAMBIO 3: Llama a la nueva función del ViewModel
    LaunchedEffect(eventId) { vm.loadEventById(eventId) }

    when {
        uiState.isLoading -> { // <-- Usa isLoading del uiState
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null -> onClose() // <-- Usa errorMessage del uiState
        uiState.event != null -> { // <-- Usa event del uiState
            val e = uiState.event!! // Accede al evento desde uiState
            LaunchedEffect(e.id) {
                navController.navigate(
                    Routes.homeWithFocus(
                        eventId = e.id.ifBlank { eventId },
                        lat = e.latitude,
                        lon = e.longitude
                    )
                ) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}
