package com.carlitoswy.flashmeet.presentation.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.datastore.OnboardingPrefs
import com.carlitoswy.flashmeet.ui.components.WelcomeAnimation
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var showLogo by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showLottie by remember { mutableStateOf(false) }
    var startExitAnim by remember { mutableStateOf(false) }
    var showOverlay by remember { mutableStateOf(false) }

    // 🎨 Fondo animado degradado dinámico
    val infiniteTransition = rememberInfiniteTransition()
    val animatedStart by infiniteTransition.animateColor(
        initialValue = Color(0xFF141E30),
        targetValue = Color(0xFF243B55),
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse)
    )
    val animatedEnd by infiniteTransition.animateColor(
        initialValue = Color(0xFF0F2027),
        targetValue = Color(0xFF2C5364),
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse)
    )

    // ⏳ Secuencia de animaciones y navegación
    LaunchedEffect(Unit) {
        if (!isPreview) {
            showLogo = true; delay(1000)
            showTagline = true; delay(1200)
            showLottie = true; delay(2500) // ⏳ dejamos ver la animación completa
            startExitAnim = true; delay(800)
            showOverlay = true; delay(500)

            // ✅ Determinar destino según onboarding y login
            val seen = OnboardingPrefs.hasSeenOnboarding(context)
            val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
            val nextRoute = when {
                !seen -> Routes.ONBOARDING
                isLoggedIn -> Routes.HOME
                else -> Routes.AUTH
            }

            navController.navigate(nextRoute) {
                popUpTo(Routes.WELCOME) { inclusive = true }
            }
        }
    }

    val bgScale by animateFloatAsState(if (startExitAnim) 1.15f else 1f, tween(800))
    val blurAmount by animateFloatAsState(if (startExitAnim) 12f else 0f, tween(800))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(animatedStart, animatedEnd)))
            .blur(blurAmount.dp)
            .graphicsLayer(scaleX = bgScale, scaleY = bgScale),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ✅ Logo
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.6f),
                exit = fadeOut(tween(400))
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_flashmeet_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(140.dp)
                )
            }

            // ✅ Eslogan
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(tween(1000)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(400))
            ) {
                Text(
                    text = "Conecta. Crea. Disfruta.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ✅ Animación Lottie
            val lottieScale by animateFloatAsState(if (startExitAnim) 0.7f else 1f, tween(600))
            AnimatedVisibility(
                visible = showLottie,
                enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.8f),
                exit = fadeOut(tween(400))
            ) {
                WelcomeAnimation(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer(scaleX = lottieScale, scaleY = lottieScale)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ✅ Loading mientras se reproduce la animación
            AnimatedVisibility(visible = showLottie && !startExitAnim) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // ✅ Overlay de salida
        AnimatedVisibility(visible = showOverlay, enter = fadeIn(tween(400))) {
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.9f)))
        }
    }
}
