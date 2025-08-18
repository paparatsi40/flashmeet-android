package com.carlitoswy.flashmeet.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.presentation.forgot.ForgotPasswordViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var exitAnim by remember { mutableStateOf(false) }

    val state by forgotPasswordViewModel.state.collectAsState()

    val invalidEmail = stringResource(R.string.invalid_email)
    val title = stringResource(R.string.forgot_password_title)
    val subtitle = stringResource(R.string.forgot_password_subtitle)
    val sendButton = stringResource(R.string.send_reset_email_button)
    val backToLogin = stringResource(R.string.back_to_login_button)
    val successMessage = stringResource(R.string.reset_email_success_message)

    // ✅ Animación de salida tras éxito
    LaunchedEffect(state.emailSent) {
        if (state.emailSent) {
            delay(1200)
            exitAnim = true
            delay(300)
            navController.popBackStack() // Regresa al login automáticamente
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        forgotPasswordViewModel.clearFlags()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = backToLogin)
                    }
                }
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = !exitAnim,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(subtitle, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                        forgotPasswordViewModel.clearFlags()
                    },
                    label = { Text(stringResource(R.string.email_hint)) },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        emailError = null
                        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailError = invalidEmail
                        } else {
                            forgotPasswordViewModel.sendResetEmail(email.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(sendButton)
                }

                Spacer(Modifier.height(16.dp))

                when {
                    state.isLoading -> CircularProgressIndicator()
                    state.emailSent -> Text(successMessage, color = MaterialTheme.colorScheme.primary)
                    state.error != null -> Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // ✅ Limpieza de flags al salir
    DisposableEffect(Unit) {
        onDispose { forgotPasswordViewModel.clearFlags() }
    }
}
