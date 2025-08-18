package com.carlitoswy.flashmeet.domain.usecase

import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
    }
}
