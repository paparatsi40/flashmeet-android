package com.carlitoswy.flashmeet.domain.usecase

import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Unit> {
        return authRepository.signInWithGoogle(idToken).mapCatching {
            // Aquí podrías guardar algo en base de datos o solo devolver éxito
            Result.success(Unit)
        }.getOrElse { error ->
            Result.failure(error)
        }
    }
}
