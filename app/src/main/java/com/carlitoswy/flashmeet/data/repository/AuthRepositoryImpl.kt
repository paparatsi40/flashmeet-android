// data/repository/AuthRepositoryImpl.kt
package com.carlitoswy.flashmeet.data.repository

import com.carlitoswy.flashmeet.domain.model.UserProfile
import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(Exception("Usuario no encontrado"))

            val profile = UserProfile(
                id = user.uid,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                uid = user.uid,
                name = user.displayName ?: ""
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSignedInUser(): UserProfile? {
        val user = firebaseAuth.currentUser ?: return null
        return UserProfile(
            id = user.uid,
            displayName = user.displayName ?: "",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString() ?: "",
            uid = user.uid,
            name = user.displayName ?: ""
        )
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        // Aquí podrías actualizar Firestore si lo deseas
        return Result.success(Unit)
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
