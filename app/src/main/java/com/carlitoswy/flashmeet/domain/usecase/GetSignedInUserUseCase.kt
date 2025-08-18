package com.carlitoswy.flashmeet.domain.usecase

import com.carlitoswy.flashmeet.domain.model.UserProfile
import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import javax.inject.Inject

class GetSignedInUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): UserProfile? {
        return repository.getSignedInUser()
    }
}
