package com.carlitoswy.flashmeet.data.repository

import com.carlitoswy.flashmeet.domain.model.UserProfile
import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(Exception("No user"))

            val profile = UserProfile(
                uid = user.uid,
                name = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                displayName = user.displayName ?: "",
                id = user.uid
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSignedInUser(): UserProfile? {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            try {
                // Aquí podrías actualizar Firestore si estás guardando el perfil ahí.
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No signed-in user"))
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
