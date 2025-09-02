package com.carlitoswy.flashmeet

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlashMeetApp : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()

        // ✅ Firebase
        FirebaseApp.initializeApp(this)
        logFcmTokenOnce() // ← imprime el token en Logcat al iniciar

        // ✅ Google Maps
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        // ✅ Stripe (PaymentSheet)
        val pk = BuildConfig.STRIPE_PUBLISHABLE_KEY
        require(pk.isNotBlank()) { "Stripe publishable key vacía. Configura STRIPE_PK_* en gradle.properties/CI." }
        PaymentConfiguration.init(this, pk)
        Log.d("StripeInit", "✅ Stripe inicializado")
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        Log.d("MapsInit", "✅ Renderer: $renderer")
    }

    /** Obtiene e imprime el FCM device token una sola vez al arranque. */
    private fun logFcmTokenOnce() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "❌ No se pudo obtener el token", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM", "✅ token=$token")
                // TODO: si quieres, envíalo a tu backend aquí.
            }
    }
}
