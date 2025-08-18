package com.carlitoswy.flashmeet.data.firebase

import com.carlitoswy.flashmeet.domain.model.UserProfile
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthService {

    private val auth = Firebase.auth
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser = _currentUser.asStateFlow()

    suspend fun firebaseAuthWithGoogle(idToken: String): UserProfile {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw Exception("User not found")

        val profile = UserProfile(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            photoUrl = firebaseUser.photoUrl?.toString().orEmpty()
        )

        _currentUser.value = profile
        return profile
    }

    fun getCurrentUserFlow() = currentUser

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
}
