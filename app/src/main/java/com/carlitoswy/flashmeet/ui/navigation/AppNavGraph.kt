package com.carlitoswy.flashmeet.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.carlitoswy.flashmeet.presentation.event.CreateEventScreen
import com.carlitoswy.flashmeet.presentation.event.EditEventScreen
import com.carlitoswy.flashmeet.presentation.event.EventDeepLinkScreen
import com.carlitoswy.flashmeet.presentation.event.EventDetailScreen
import com.carlitoswy.flashmeet.presentation.event.FavoritesScreen
import com.carlitoswy.flashmeet.presentation.event.MyEventsScreen
import com.carlitoswy.flashmeet.presentation.event.SearchScreen
import com.carlitoswy.flashmeet.presentation.flyer.CreateFlyerScreen
import com.carlitoswy.flashmeet.presentation.flyer.FlyerEditorViewModel
import com.carlitoswy.flashmeet.presentation.flyer.FlyerListScreen
import com.carlitoswy.flashmeet.presentation.home.HomeScreen
import com.carlitoswy.flashmeet.presentation.location.LocationPermissionScreen
import com.carlitoswy.flashmeet.presentation.map.MapScreen
import com.carlitoswy.flashmeet.presentation.onboarding.OnboardingScreen
import com.carlitoswy.flashmeet.presentation.payment.StripePaymentScreen
import com.carlitoswy.flashmeet.presentation.welcome.WelcomeScreen
import com.carlitoswy.flashmeet.ui.screens.CameraCaptureScreen
import com.carlitoswy.flashmeet.ui.screens.EmailLoginScreen
import com.carlitoswy.flashmeet.ui.screens.FlyerEditorScreen
import com.carlitoswy.flashmeet.ui.screens.ForgotPasswordScreen
import com.carlitoswy.flashmeet.ui.screens.PhotoPreviewScreen
import com.carlitoswy.flashmeet.ui.screens.ProfileScreen
import com.carlitoswy.flashmeet.ui.screens.RegisterScreen
import com.carlitoswy.flashmeet.ui.screens.SplashScreen
import com.carlitoswy.flashmeet.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
    ) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.WELCOME) { WelcomeScreen(navController) }
        composable(Routes.ONBOARDING) { OnboardingScreen(navController) }

        navigation(startDestination = Routes.LOGIN, route = Routes.AUTH) {
            composable(Routes.LOGIN) {
                com.carlitoswy.flashmeet.presentation.login.LoginScreen(navController)
            }
            composable(Routes.LOGIN_EMAIL) {
                EmailLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    navController = navController,
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }
        }

        // HOME normal
        composable(Routes.HOME) { HomeScreen(navController) }

        // HOME con foco (deeplinks → centrar mapa + abrir detalle)
        composable(
            route = Routes.HOME_FOCUS,
            arguments = listOf(
                navArgument("focusId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("lon") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val focusId = backStackEntry.arguments?.getString("focusId")
            val focusLat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val focusLon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()

            HomeScreen(
                navController = navController,
                initialFocusId = focusId,
                initialFocusLat = focusLat,
                initialFocusLon = focusLon
            )
        }

        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.MAP) { MapScreen(navController) }
        composable(Routes.PERMISSION) {
            LocationPermissionScreen {
                navController.navigate(Routes.MAP) {
                    popUpTo(Routes.PERMISSION) { inclusive = true }
                }
            }
        }

        // ✅ Eventos
        composable(Routes.CREATE_EVENT) { CreateEventScreen(navController) { navController.popBackStack() } }
        composable(Routes.MY_EVENTS) { MyEventsScreen(navController) }
        composable(Routes.FAVORITES) { FavoritesScreen(navController) }

        composable(
            route = "${Routes.EVENT_DETAIL}/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            EventDetailScreen(navController, it.arguments?.getString("eventId") ?: "")
        }

        composable(
            route = "${Routes.EDIT_EVENT}/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            EditEventScreen(navController, it.arguments?.getString("eventId") ?: "")
        }

        // ✅ Deeplinks → cargar evento y redirigir a HOME con foco
        composable(
            route = Routes.EVENT,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
                // Si algún día quieres lat/lon como query, puedes añadir:
                // navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
                // navArgument("lon") { type = NavType.StringType; nullable = true; defaultValue = null }
            ),
            deepLinks = listOf(
                // --- HTTPS dominio principal ---
                navDeepLink { uriPattern = "https://flashmeet.app/event/{eventId}" },
                navDeepLink { uriPattern = "https://flashmeet.app/e/{eventId}" },

                // --- HTTPS con www (🆕 añadido) ---
                navDeepLink { uriPattern = "https://www.flashmeet.app/event/{eventId}" },
                navDeepLink { uriPattern = "https://www.flashmeet.app/e/{eventId}" },

                // --- Esquema personalizado (fallback/testing) ---
                navDeepLink { uriPattern = "flashmeet://event/{eventId}" }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDeepLinkScreen(
                eventId = eventId,
                navController = navController,
                onClose = { navController.popBackStack() }
            )
        }

        // ✅ Flyers
        composable(Routes.FLYER_LIST) { FlyerListScreen(navController) }
        composable(Routes.FLYER_EDITOR) {
            val vm = hiltViewModel<FlyerEditorViewModel>()
            FlyerEditorScreen(navController, vm)
        }
        composable(Routes.CREATE_FLYER) { CreateFlyerScreen(navController) { navController.popBackStack() } }

        // ✅ Otros
        composable(Routes.PAYMENT) { StripePaymentScreen(navController) }
        composable(Routes.SEARCH) { SearchScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable("camera_capture") {
            CameraCaptureScreen(
                onImageCaptured = { uri ->
                    navController.navigate("${Routes.PHOTO_PREVIEW}/${Uri.encode(uri.toString())}")
                },
                onError = { exc ->
                    Log.e("CameraNav", "Camera capture error: ${exc.message}", exc)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "${Routes.PHOTO_PREVIEW}/{photoUri}",
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) {
            it.arguments?.getString("photoUri")?.toUri()?.let { uri ->
                PhotoPreviewScreen(navController, uri)
            }
        }
    }
}
