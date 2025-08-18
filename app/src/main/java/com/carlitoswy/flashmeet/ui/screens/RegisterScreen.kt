package com.carlitoswy.flashmeet.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.presentation.login.AuthViewModel
import com.carlitoswy.flashmeet.ui.components.RegisterAnimation
import kotlinx.coroutines.delay


@Composable
fun RegisterScreen(
    navController: NavHostController, // <--- Add this parameter!
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by authViewModel.authState.collectAsState()

    // Campos de entrada
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    // Errores de validación de campos
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var repeatError by remember { mutableStateOf<String?>(null) }

    // Control de animación de entrada/salida de la pantalla
    var visible by remember { mutableStateOf(false) }
    var exitAnim by remember { mutableStateOf(false) }

    // OBTENER EL CONTEXTO AQUÍ, FUERA DEL ONCLICK, DONDE SÍ ES CONTEXTO COMPOSABLE
    val context = LocalContext.current // <--- ¡Añadir esta línea!

    // Iniciar animación de entrada al componer
    LaunchedEffect(Unit) { delay(200); visible = true }

    // Navegación cuando el registro es exitoso, con animación de salida
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            exitAnim = true
            delay(400)
            onRegisterSuccess() // ✅ este llama al navController en el AuthGraph
        }
    }





    // Contenedor principal con animación de entrada y salida de la pantalla
    AnimatedVisibility(
        visible = visible && !exitAnim,
        enter = slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(600, easing = FastOutSlowInEasing)) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) + fadeOut()
    ) {
        // Box principal de la pantalla, que contendrá el contenido y el overlay de carga
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Contenido principal de la pantalla de registro (campos, botones, etc.)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🎬 Animación del logo Lottie escalado
                val logoScale by animateFloatAsState(if (visible) 1f else 0.7f, tween(700))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RegisterAnimation(modifier = Modifier.size((150 * logoScale).dp))
                }

                Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)

                // 📧 Campo de Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = { Text(stringResource(R.string.email_hint)) },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // 🔑 Campo de Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null; repeatError = null },
                    label = { Text(stringResource(R.string.password_hint)) },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // ✅ Campo de Repetir Contraseña
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it; repeatError = null },
                    label = { Text(stringResource(R.string.repeat_password_hint)) },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = repeatError != null,
                    supportingText = { repeatError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // 🔥 Botón de Registro
                Button(
                    onClick = {
                        // Limpiar errores previos
                        emailError = null; passwordError = null; repeatError = null
                        authViewModel.setError(null) // Limpiar errores del ViewModel

                        var hasError = false // Flag para controlar si hay errores de validación

                        // Validaciones de los campos - ¡USAR context.getString() AQUÍ!
                        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailError = context.getString(R.string.invalid_email) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        }
                        if (password.isBlank()) {
                            passwordError = context.getString(R.string.password_required) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        } else if (password.length < 6) {
                            passwordError = context.getString(R.string.password_length_error) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        } else if (!password.any { it.isUpperCase() }) {
                            passwordError = context.getString(R.string.password_uppercase_required) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        } else if (!password.any { it.isLowerCase() }) {
                            passwordError = context.getString(R.string.password_lowercase_required) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        } else if (!password.any { it.isDigit() }) {
                            passwordError = context.getString(R.string.password_digit_required) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        } else if (!password.any { !it.isLetterOrDigit() }) { // Carácter especial
                            passwordError = context.getString(R.string.password_special_char_required) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        }
                        if (password != repeatPassword) {
                            repeatError = context.getString(R.string.passwords_do_not_match) // <--- ¡CAMBIO AQUÍ!
                            hasError = true
                        }

                        // Si no hay errores de validación, intentar registrar
                        if (!hasError) authViewModel.registerWithEmail(email.trim(), password.trim())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.register_button))
                }

                // Botón para volver a la pantalla anterior (Login)
                TextButton(onClick = onBack) { Text(stringResource(R.string.already_have_account)) }

                // ❗ Mostrar errores globales del backend (si no hay errores de campo específicos)
                state.error?.takeIf { emailError == null && passwordError == null && repeatError == null }?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            } // Fin de la Column que contiene los elementos de la pantalla

            // ⏳ Overlay de Carga (cuando state.isLoading es true)
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)), // Fondo semi-transparente
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White) // Indicador de carga blanco
                }
            }
        } // Fin del Box principal
    } // Fin de AnimatedVisibility
}
