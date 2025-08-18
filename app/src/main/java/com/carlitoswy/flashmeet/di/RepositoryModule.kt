// Archivo: app/src/main/java/com/carlitoswy/flashmeet/di/RepositoryModule.kt

package com.carlitoswy.flashmeet.di

import com.carlitoswy.flashmeet.data.repository.FirestoreEventRepository
import com.carlitoswy.flashmeet.data.repository.FlashEventRepositoryImpl
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.carlitoswy.flashmeet.domain.repository.FlashEventRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule { // Es un 'object' y usa @Provides

    @Provides
    @Singleton
    fun provideFlashEventRepository(
        firestore: FirebaseFirestore
    ): FlashEventRepository {
        // Hilt inyectará firestore aquí porque ya sabe cómo proporcionarlo (o está en otro módulo)
        return FlashEventRepositoryImpl(firestore)
    }

    // AÑADE ESTE NUEVO MÉTODO @Provides para EventRepository
    @Provides
    @Singleton
    fun provideEventRepository(
        // Hilt inyectará FirestoreEventRepository aquí porque su constructor tiene @Inject
        // y Hilt sabe cómo proporcionar sus dependencias (FirebaseFirestore).
        firestoreEventRepository: FirestoreEventRepository
    ): EventRepository {
        return firestoreEventRepository
    }

    // NOTA: No necesitas un @Provides para FirebaseFirestore aquí,
    // porque ya estás aceptando 'firestore: FirebaseFirestore' en provideFlashEventRepository,
    // lo que le dice a Hilt que debe saber cómo proporcionar FirebaseFirestore.
    // Si no fuera así, necesitarías:
    /*
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    */
}
