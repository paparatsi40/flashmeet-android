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
 * 2) Si existen lat/lon en el deeplink, las prioriza; si no, usa las del evento.
 * 3) Navega a HOME con foco en el evento (y centrado si hay coords).
 * 4) Si hay error, cierra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDeepLinkScreen(
    eventId: String,
    navController: NavController,
    lat: Double? = null,                 // 💡 coords opcionales desde el deeplink
    lon: Double? = null,
    onClose: () -> Unit,
    vm: EventDetailViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    // Cargar el evento por ID
    LaunchedEffect(eventId) { vm.loadEventById(eventId) }

    when {
        uiState.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null -> onClose()
        uiState.event != null -> {
            val e = uiState.event!!
            // Prioriza coords del deeplink; si no hay, usa las del evento
            val targetLat = lat ?: e.latitude
            val targetLon = lon ?: e.longitude
            val focusId = e.id.ifBlank { eventId }

            LaunchedEffect(focusId, targetLat, targetLon) {
                navController.navigate(
                    Routes.homeWithFocus(
                        eventId = focusId,
                        lat = targetLat,          // puede ser null; tu builder debe soportarlo
                        lon = targetLon
                    )
                ) {
                    // Limpia para que al back no vuelva a esta pantalla intermedia
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}
