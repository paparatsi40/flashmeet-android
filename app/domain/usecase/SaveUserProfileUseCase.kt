package com.carlitoswy.flashmeet.domain.usecase

import com.carlitoswy.flashmeet.domain.model.UserProfile
import com.carlitoswy.flashmeet.data.repository.AuthRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<UserProfile> {
        return authRepository.signInWithGoogle(idToken)
    }
}
