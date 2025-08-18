package com.carlitoswy.flashmeet.presentation.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.carlitoswy.flashmeet.permissions.LocationPermissionHandler
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavHostController, viewModel: MapViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()
    var fabExpanded by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val events by viewModel.events.collectAsState()

// Pedimos permisos
    LocationPermissionHandler(
        onPermissionGranted = {
            locationPermissionGranted = true
            viewModel.fetchEvents()
        },
        onPermissionDenied = { locationPermissionGranted = false }
    )

// Ubicación del usuario y movimiento de cámara
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                @SuppressLint("MissingPermission")
                val location = fusedLocationClient.lastLocation.await()
                userLocation = location?.let { LatLng(it.latitude, it.longitude) }

                userLocation?.let {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
                } ?: run {
                    Log.w("MapScreen", "Última ubicación es nula, mapa centrado en default.")
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(0.0, 0.0),
                            1f
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error obteniendo ubicación en MapScreen: ${e.message}", e)
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f))
            }
        } else {
            Log.d(
                "MapScreen",
                "Permiso de ubicación no concedido para MapScreen. Centrando en default."
            )
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f))
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa en el fondo
        GoogleMap(
            modifier = Modifier
                .matchParentSize()
                .zIndex(0f), // 👈 asegúrate de que el mapa esté detrás
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            properties = MapProperties(isMyLocationEnabled = locationPermissionGranted)
        ) {
            userLocation?.let { // ✨✨ ESTE BLOQUE ES SOLO PARA EL MARCADOR DEL USUARIO ✨✨
                Marker(
                    state = MarkerState(position = it),
                    title = "Tu ubicación",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            } // ✨✨ FIN DEL BLOQUE PARA EL USUARIO ✨✨


            events.forEach { event ->
                val eventLatLng = LatLng(event.latitude, event.longitude)

                Marker(
                    state = MarkerState(position = eventLatLng),
                    title = event.title,
                    snippet = event.category.toString(),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                    onClick = {
                        // Aquí puedes ir a detalle
                        navController.navigate("${Routes.EVENT_DETAIL}/${event.id}")
                        true // Retorna true para consumir el evento click
                    }
                )
            }
        }



        // FABs encima del mapa
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
                .zIndex(1f), // 👈 esto asegura que estén por ENCIMA del mapa
            horizontalAlignment = Alignment.End
        ) {
            if (fabExpanded) {
                FloatingActionButton(
                    onClick = {
                        fabExpanded = false
                        navController.navigate(Routes.SEARCH)
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar eventos")
                }

                Spacer(modifier = Modifier.height(12.dp))

                FloatingActionButton(
                    onClick = {
                        fabExpanded = false
                        navController.navigate(Routes.CREATE_EVENT)
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear evento")
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = "Menú de acciones"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        userLocation?.let {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(it, 15f)
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(Icons.Outlined.GpsFixed, contentDescription = "Centrar en ubicación")
            }


        }
    }
}



// ✨✨ EventMarker ya no es necesario si usas el dominio.model.Event directamente ✨✨
// Puedes eliminar o comentar esta data class si no se usa en ningún otro lugar
/*
data class EventMarker(
    val id: String,
    val title: String,
    val latLng: LatLng
)
*/
