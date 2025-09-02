package com.carlitoswy.flashmeet.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.carlitoswy.flashmeet.presentation.login.LoginScreen
import com.carlitoswy.flashmeet.ui.screens.EmailLoginScreen
import com.carlitoswy.flashmeet.ui.screens.ForgotPasswordScreen
import com.carlitoswy.flashmeet.ui.screens.RegisterScreen

fun NavGraphBuilder.AuthNavGraph(navController: androidx.navigation.NavHostController) {
    navigation(startDestination = Routes.LOGIN, route = Routes.AUTH) {

        composable(Routes.LOGIN) { LoginScreen(navController) }

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
                navController = navController, // Pass the navController here
                onRegisterSuccess = { /* handle success, perhaps navigate */ },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }
    }
}
