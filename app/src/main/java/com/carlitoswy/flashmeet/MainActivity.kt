package com.carlitoswy.flashmeet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.carlitoswy.flashmeet.datastore.OnboardingPrefs
import com.carlitoswy.flashmeet.localization.LocalAppLanguage
import com.carlitoswy.flashmeet.ui.navigation.AppNavGraph
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.carlitoswy.flashmeet.ui.theme.FlashMeetTheme
import com.carlitoswy.flashmeet.utils.LocaleManager
import com.carlitoswy.flashmeet.utils.PendingEventManager
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Inicializaciones Firebase
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        PaymentConfiguration.init(applicationContext, "pk_test_51KgF4kIHad7GoCUdlt2ifslj3nKHu4ad79MwhvKpQBVJrG8qRKE7z1fCfcYOdJkwWiL48STKu030006fulz3O")
        if (!Places.isInitialized()) Places.initialize(applicationContext, getString(R.string.google_maps_key))

        // ✅ Captura el eventId de notificaciones y lo guarda
        intent?.getStringExtra("eventId")?.let { PendingEventManager.savePendingEvent(this, it) }

        // ✅ Obtén y envía el Token FCM al servidor
        fetchAndUploadFcmToken(this)

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            val appLang = remember { mutableStateOf(context.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("language", "es") ?: "es") }

            val startDestination by produceState<String?>(initialValue = null) {
                val seenOnboarding = OnboardingPrefs.hasSeenOnboarding(context)
                val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
                value = when {
                    !seenOnboarding -> Routes.WELCOME
                    isLoggedIn -> Routes.HOME
                    else -> Routes.AUTH
                }
            }

            CompositionLocalProvider(LocalAppLanguage provides appLang) {
                FlashMeetTheme {
                    startDestination?.let { destination ->
                        AppNavGraph(navController, startDestination = destination)

                        // ✅ Si había un evento pendiente, navega directo al detalle
                        LaunchedEffect(destination) {
                            val pendingEventId = PendingEventManager.consumePendingEvent(context)
                            if (!pendingEventId.isNullOrEmpty()) {
                                if (destination == Routes.HOME) {
                                    navController.navigate("${Routes.EVENT_DETAIL}/$pendingEventId") {
                                        popUpTo(Routes.HOME) { inclusive = false }
                                    }
                                } else {
                                    navController.addOnDestinationChangedListener { _, dest, _ ->
                                        if (dest.route == Routes.HOME) {
                                            navController.navigate("${Routes.EVENT_DETAIL}/$pendingEventId") {
                                                popUpTo(Routes.HOME) { inclusive = false }
                                            }
                                            PendingEventManager.clearPendingEvent(context)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ✅ Botón Crashlytics
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                        Button(onClick = { throw RuntimeException("💥 Crash de prueba de FlashMeet!") }) { Text("Forzar Crash") }
                    }
                }
            }
        }
    }

    /** ✅ Obtiene el Token FCM y lo guarda en Firestore */
    private fun fetchAndUploadFcmToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "❌ Error obteniendo token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TOKEN", "🔥 Token FCM: $token")
            Toast.makeText(context, "Token FCM obtenido", Toast.LENGTH_SHORT).show()

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(user.uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener { Log.d("FCM_TOKEN", "✅ Token guardado en Firestore") }
                    .addOnFailureListener { e -> Log.e("FCM_TOKEN", "❌ Error guardando token: ${e.message}") }
            }
        }
    }
}
