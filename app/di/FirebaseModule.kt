package com.carlitoswy.flashmeet.di

import com.carlitoswy.flashmeet.data.repository.AuthRepositoryImpl
import com.carlitoswy.flashmeet.data.repository.EventRepositoryImpl
import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideEventRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): EventRepository = EventRepositoryImpl(firestore, auth)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth)
}
