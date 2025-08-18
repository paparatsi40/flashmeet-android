package com.carlitoswy.flashmeet.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.privacysandbox.tools.core.generator.build
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext // Necesario para Hilt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject // Necesario para Hilt
import javax.inject.Singleton // Opcional, pero bueno para servicios

@Singleton // Para que Hilt proporcione la misma instancia
class LocationService @Inject constructor( // Constructor inyectado por Hilt
    @ApplicationContext private val context: Context // Hilt proveerá el contexto de la aplicación
) {

    private val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // Los permisos se deben verificar ANTES de llamar a esta función
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000L) // 5 segundos de intervalo deseado
            .setMinUpdateIntervalMillis(2000L) // 2 segundos de intervalo mínimo
            .build()

        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let {
                    Log.d("LocationService_Callback", "onLocationResult: Lat ${it.latitude}, Lng ${it.longitude}, Acc: ${it.accuracy}")
                    trySend(it).isSuccess
                } ?: Log.w("LocationService_Callback", "onLocationResult: lastLocation es null")
            }
        }

        // Esta línea puede fallar si los permisos no están concedidos.
        // El llamador (ViewModel) es responsable de verificar los permisos.
        fusedLocationClient.requestLocationUpdates(locationRequest, callback, null)
            .addOnSuccessListener {
                Log.d("LocationService", "Solicitud de actualizaciones INICIADA con éxito.")
            }
            .addOnFailureListener { e ->
                Log.e("LocationService", "FALLO al iniciar actualizaciones: ${e.message}")
                android.system.Os.close(e)
            }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}