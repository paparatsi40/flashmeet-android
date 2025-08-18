// di/AuthModule.kt
package com.carlitoswy.flashmeet.di

import com.carlitoswy.flashmeet.data.repository.AuthRepositoryImpl
import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
