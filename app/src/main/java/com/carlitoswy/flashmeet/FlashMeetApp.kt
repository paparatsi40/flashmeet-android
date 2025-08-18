package com.carlitoswy.flashmeet

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.firebase.FirebaseApp
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlashMeetApp : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()

        // Firebase
        FirebaseApp.initializeApp(this)

        // Google Maps
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        // Stripe (PaymentSheet)
        val pk = BuildConfig.STRIPE_PUBLISHABLE_KEY
        require(pk.isNotBlank()) { "Stripe publishable key vacía. Configura STRIPE_PK_* en gradle.properties/CI." }
        PaymentConfiguration.init(this, pk)
        Log.d("StripeInit", "✅ Stripe inicializado")
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        Log.d("MapsInit", "✅ Renderer: $renderer")
    }
}
