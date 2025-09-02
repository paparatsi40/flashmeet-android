package com.carlitoswy.flashmeet.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.presentation.home.centerOnLocation
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RadarFab(
    navController: NavController,
    expanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    userLocation: Pair<Double, Double>?,
    cameraPositionState: CameraPositionState,
    scope: CoroutineScope
) {
    Column(
        modifier = Modifier
            .padding(end = 16.dp, bottom = 96.dp)
            .zIndex(1f)
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomEnd), // ✅ CORREGIDO
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.6f)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = {
                        onExpandChanged(false)
                        navController.navigate(Routes.SEARCH)
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar eventos")
                }

                Spacer(modifier = Modifier.height(12.dp))

                FloatingActionButton(
                    onClick = {
                        onExpandChanged(false)
                        navController.navigate(Routes.CREATE_EVENT)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear evento")
                }

                Spacer(modifier = Modifier.height(12.dp))

                FloatingActionButton(
                    onClick = {
                        onExpandChanged(false)
                        userLocation?.let { (lat, lon) ->
                            scope.launch {
                                cameraPositionState.centerOnLocation(lat, lon)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar ubicación")
                }
            }
        }

        val rotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            label = "fabRotation"
        )

        FloatingActionButton(
            onClick = { onExpandChanged(!expanded) },
            modifier = Modifier.graphicsLayer { rotationZ = rotation }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Menú")
        }
    }
}
