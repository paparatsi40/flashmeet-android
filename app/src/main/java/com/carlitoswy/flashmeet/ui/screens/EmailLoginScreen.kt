package com.carlitoswy.flashmeet.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carlitoswy.flashmeet.presentation.login.AuthViewModel

@Composable
fun EmailLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel() // Uso directo de hiltViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estado local para errores de validación de campos
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

    // Observa los cambios en el email o password para limpiar errores de Firebase del ViewModel
    LaunchedEffect(email, password) {
        if (authState.error != null) {
            authViewModel.setError(null) // Limpia el error del ViewModel cuando el usuario edita los campos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null // Limpiar error al escribir
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null || (authState.error != null && email.isNotBlank()), // Error visual si hay error local o de Firebase
            supportingText = {
                emailError?.let { Text(it) } // Muestra el error local
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null // Limpiar error al escribir
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null || (authState.error != null && password.isNotBlank()), // Error visual si hay error local o de Firebase
            supportingText = {
                passwordError?.let { Text(it) } // Muestra el error local
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Reiniciar errores locales
                emailError = null
                passwordError = null
                authViewModel.setError(null) // Limpiar errores previos del ViewModel al intentar login

                // Validaciones previas a la llamada al ViewModel
                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = "Formato de correo electrónico inválido."
                }
                if (password.isBlank()) {
                    passwordError = "La contraseña no puede estar vacía."
                }

                // Si no hay errores locales, intentar el login
                if (emailError == null && passwordError == null) {
                    authViewModel.signInWithEmail(email.trim(), password.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Volver")
        }

        // Mostrar errores del ViewModel (Firebase) si existen y no hay errores locales en los campos
        authState.error?.let {
            // Este texto se muestra si hay un error de Firebase Y NO hay un error local ya cubierto por supportingText
            val displayError = (emailError == null && passwordError == null)
            if (displayError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        if (authState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }

    // Observa el cambio de estado para navegar
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginSuccess()
        }
    }
}
