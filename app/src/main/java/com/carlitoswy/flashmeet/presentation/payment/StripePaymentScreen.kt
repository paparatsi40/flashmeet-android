package com.carlitoswy.flashmeet.presentation.payment

// REMOVED: androidx.lifecycle.viewmodel.compose.viewModel // Ya no se usa si todo es hiltViewModel
// REMOVED: import com.stripe.android.paymentsheet.PaymentSheet // No se necesita importar directamente si usas rememberPaymentSheet
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.carlitoswy.flashmeet.presentation.flyer.CreateFlyerViewModel
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

@Composable
fun StripePaymentScreen(
    navController: NavHostController,
    // MODIFIED: Usar hiltViewModel() para CreateFlyerViewModel también
    createFlyerViewModel: CreateFlyerViewModel = hiltViewModel(),
    stripeViewModel: StripePaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // Es buena práctica asegurarse de que el contexto es una ComponentActivity
    val activity = context as? ComponentActivity
        ?: error("StripePaymentScreen debe estar en Activity que herede de ComponentActivity")

    // Initialize Stripe SDK with the publishable key
    LaunchedEffect(Unit) {
        PaymentConfiguration.init(context, stripeViewModel.publishableKey)
        stripeViewModel.fetchPaymentIntent()
    }

    val clientSecret by stripeViewModel.clientSecret.collectAsState()
    val statusText by stripeViewModel.statusText.collectAsState()
    val isLoading by stripeViewModel.isLoading.collectAsState()

    // MODIFIED: Usar rememberPaymentSheet para obtener el PaymentSheetLauncher
    val paymentSheetLauncher = rememberPaymentSheet(
        paymentResultCallback = { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    stripeViewModel.setStatus("✅ Pago exitoso")
                    // MODIFIED: Llamar a la función correcta en CreateFlyerViewModel
                    // que ahora se encargará de guardar el flyer en Firestore/Storage
                    createFlyerViewModel.finalizeFlyerAfterPayment {
                        // Después de que el flyer se haya finalizado correctamente,
                        // podemos navegar a una pantalla de éxito o regresar.
                        // Ejemplo: Navegar a la pantalla principal (HOME)
                        // popUpTo te ayuda a limpiar el stack de navegación si lo necesitas.
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true } // Limpia el stack hasta HOME
                        }
                    }
                }
                is PaymentSheetResult.Canceled ->
                    stripeViewModel.setStatus("⚠️ Pago cancelado")
                is PaymentSheetResult.Failed ->
                    // El error contiene detalles sobre por qué falló el pago.
                    stripeViewModel.setStatus("❌ Error: ${result.error.localizedMessage}")
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                clientSecret?.let { secret ->
                    // Usar el launcher obtenido de rememberPaymentSheet para presentar la hoja de pago
                    paymentSheetLauncher.presentWithPaymentIntent(
                        paymentIntentClientSecret = secret,
                        configuration = com.stripe.android.paymentsheet.PaymentSheet.Configuration(
                            merchantDisplayName = "FlashMeet Inc."
                            // Puedes añadir más configuraciones aquí, como la recolección de detalles
                            // del cliente, dirección de facturación, etc.
                        )
                    )
                }
            },
            // El botón estará habilitado solo si tenemos el clientSecret, no estamos cargando
            // y la actividad es válida.
            enabled = clientSecret != null && !isLoading && activity != null
        ) {
            Text("Pagar ahora")
        }

        if (isLoading) CircularProgressIndicator()
        if (statusText.isNotEmpty()) Text(statusText)
    }
}
