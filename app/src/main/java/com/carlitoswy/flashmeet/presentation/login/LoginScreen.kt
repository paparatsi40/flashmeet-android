package com.carlitoswy.flashmeet.presentation.login

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var exitAnim by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            task.getResult(ApiException::class.java)?.idToken?.let(viewModel::signInWithGoogle)
                ?: viewModel.setError(context.getString(R.string.auth_error_google_token_null))
        } catch (e: ApiException) {
            viewModel.setError(context.getString(R.string.auth_error_google_signin_failed, e.localizedMessage ?: "Error"))
        }
    }

    val signInClient = remember {
        GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        )
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            exitAnim = true
            delay(400)
            navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
        }
    }

    AnimatedVisibility(visible = !exitAnim, enter = fadeIn() + scaleIn(initialScale = 0.9f), exit = fadeOut()) {
    Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(Modifier.size(100.dp), CircleShape, MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text("FM", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(email, { email = it; emailError = null; viewModel.setError(null) },
                label = { Text(stringResource(R.string.email_hint)) }, isError = emailError != null,
                supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(password, { password = it; passwordError = null; viewModel.setError(null) },
                label = { Text(stringResource(R.string.password_hint)) },
                visualTransformation = PasswordVisualTransformation(), isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    emailError = null; passwordError = null; viewModel.setError(null)
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) emailError = context.getString(R.string.invalid_email)
                    else if (password.isBlank()) passwordError = context.getString(R.string.password_required)
                    if (emailError == null && passwordError == null) viewModel.signInWithEmail(email.trim(), password.trim())
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.login_button)) }

            Spacer(Modifier.height(8.dp))
            TextButton({ navController.navigate(Routes.FORGOT_PASSWORD) }) { Text(stringResource(R.string.forgot_password)) }
            TextButton({ navController.navigate(Routes.REGISTER) }) { Text(stringResource(R.string.register_prompt)) }

            Spacer(Modifier.height(8.dp))
            Button({ launcher.launch(signInClient.signInIntent) },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painterResource(R.drawable.ic_google_logo), "Google", tint = Color.Unspecified)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.google_signin))
            }

            if (state.isLoading) CircularProgressIndicator(Modifier.padding(8.dp))
            state.error?.let { if (emailError == null && passwordError == null) Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
