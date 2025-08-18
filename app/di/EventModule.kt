package com.carlitoswy.flashmeet.di

import com.carlitoswy.flashmeet.data.remote.EventService
import com.carlitoswy.flashmeet.data.repository.EventRepositoryImpl
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideEventRepository(firestore: FirebaseFirestore): EventRepository =
        EventRepositoryImpl(firestore)

}
