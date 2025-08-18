package com.carlitoswy.flashmeet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.datastore.OnboardingPrefs
import com.carlitoswy.flashmeet.ui.components.SplashAnimation
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    var showContent by remember { mutableStateOf(true) }
    var startExitAnim by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startExitAnim) 1.2f else 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "scaleOut"
    )

    LaunchedEffect(Unit) {
        delay(3000)
        startExitAnim = true
        delay(500)
        showContent = false
        delay(300)

        val hasSeenOnboarding = OnboardingPrefs.hasSeenOnboarding(context)
        when {
            !hasSeenOnboarding -> navController.navigate(Routes.ONBOARDING) {
                popUpTo(0) { inclusive = true }
            }
            currentUser == null -> navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
            else -> navController.navigate(Routes.HOME) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    AnimatedVisibility(
        visible = showContent,
        exit = fadeOut(tween(400)) + scaleOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SplashAnimation(modifier = Modifier)
                Spacer(Modifier.height(24.dp))
                Text(text = stringResource(R.string.splash_title), style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}
