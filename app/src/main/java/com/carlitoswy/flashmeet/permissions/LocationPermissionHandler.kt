package com.carlitoswy.flashmeet.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

enum class LocationPermissionResult {
    GRANTED, DENIED, PERMANENTLY_DENIED
}

@Composable
fun LocationPermissionHandler(
    // ✨✨ CAMBIO CLAVE AQUÍ: Los lambdas NO son @Composable ✨✨
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {} // También este no es @Composable
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // ✨✨ Se mueve la lógica de notificación a un LaunchedEffect que monitorea permissionResult ✨✨
    var permissionResult by remember { mutableStateOf<LocationPermissionResult?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionResult = if (isGranted) {
            LocationPermissionResult.GRANTED
        } else {
            // Verifica si el usuario marcó "No volver a preguntar"
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                LocationPermissionResult.PERMANENTLY_DENIED
            } else {
                LocationPermissionResult.DENIED
            }
        }
    }

    // ✨✨ Este LaunchedEffect lanza la solicitud de permiso al inicio del Composable ✨✨
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            permissionResult = LocationPermissionResult.GRANTED
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ✨✨ CORRECCIÓN CLAVE: Mover las llamadas a los lambdas fuera del LaunchedEffect ✨✨
    // Simplemente llamamos a los lambdas según el estado del permiso.
    // Esto se ejecutará cada vez que permissionResult cambie.
    when (permissionResult) {
        LocationPermissionResult.GRANTED -> onPermissionGranted()
        LocationPermissionResult.DENIED -> onPermissionDenied()
        LocationPermissionResult.PERMANENTLY_DENIED -> {
            context.openAppSettings() // Abre la configuración de la app
            onPermissionDenied() // Notifica al padre que el permiso fue denegado (permanente)
        }
        null -> { /* Esperando resultado del permiso */ }
    }
}

// Función de extensión para abrir la configuración de la app
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
