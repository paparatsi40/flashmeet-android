package com.carlitoswy.flashmeet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    navController: NavController,
    // user: FirebaseUser? = FirebaseAuth.getInstance().currentUser, // You can keep this or remove the default, as we're getting user inside
    modifier: Modifier = Modifier
    // !! IMPORTANT !! The 'onLogout' parameter MUST NOT be here anymore.
    // If you see 'onLogout: () -> Unit = {}' remove that entire line.
) {
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser // Get user here

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user?.photoUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(user.photoUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Bienvenido, ${user?.displayName ?: "Usuario"}!",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            // Sign out the user from Firebase Authentication
            FirebaseAuth.getInstance().signOut()

            // Navigate back to the authentication flow
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }) {
            Text("Cerrar sesión")
        }
    }
}
