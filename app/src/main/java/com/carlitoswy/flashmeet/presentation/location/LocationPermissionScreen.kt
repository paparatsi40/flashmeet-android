package com.carlitoswy.flashmeet.presentation.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carlitoswy.flashmeet.permissions.LocationPermissionHandler // <--- IMPORTA TU HANDLER
import com.carlitoswy.flashmeet.permissions.openAppSettings // <--- IMPORTA LA EXTENSIÓN

@Composable
fun LocationPermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var permissionStatusMessage by remember { mutableStateOf("Solicitando permisos de ubicación...") }
    var showGoToSettingsButton by remember { mutableStateOf(false) }

    // Tu LocationPermissionHandler se encarga de la lógica de pedir el permiso.
    // Nosotros reaccionamos a sus callbacks.
    LocationPermissionHandler(
        onPermissionGranted = {
            permissionStatusMessage = "¡Permiso de ubicación concedido!"
            showGoToSettingsButton = false // Oculta el botón si se concede
            onPermissionGranted() // Llama al callback para navegar en NavGraph
        },
        onPermissionDenied = { // Este callback se activa si es DENIED o PERMANENTLY_DENIED (después de abrir settings)
            permissionStatusMessage = "Permiso de ubicación denegado. Es necesario para usar la aplicación."
            showGoToSettingsButton = true
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = permissionStatusMessage,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        if (showGoToSettingsButton) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { context.openAppSettings() }) {
                Text("Ir a Configuración de la Aplicación")
            }
        }
    }
}
