// domain/repository/AuthRepository.kt
package com.carlitoswy.flashmeet.domain.repository

import com.carlitoswy.flashmeet.domain.model.UserProfile

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile>
    suspend fun getSignedInUser(): UserProfile?
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun signOut()
}
