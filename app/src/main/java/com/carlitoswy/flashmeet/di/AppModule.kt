package com.carlitoswy.flashmeet.di

// import com.google.firebase.firestore.FirebaseFirestore // Esta importación puede irse si ya no se usa aquí
import android.content.Context
import com.carlitoswy.flashmeet.data.location.LocationService
import com.carlitoswy.flashmeet.domain.location.GetCurrentLocationUseCase
import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.carlitoswy.flashmeet.domain.usecase.GetSignedInUserUseCase
import com.carlitoswy.flashmeet.domain.usecase.SignInWithGoogleUseCase
import com.carlitoswy.flashmeet.domain.usecase.SignOutUseCase
import com.carlitoswy.flashmeet.domain.usecase.event.GetEventByIdUseCase
import com.carlitoswy.flashmeet.domain.usecase.event.UpdateEventUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Ya no hay @Provides para FirebaseFirestore aquí, lo provee FirebaseModule
    // Ya no hay @Provides para FirebaseAuth aquí, lo provee FirebaseModule
    // Ya no hay @Provides para AuthRepository aquí, lo provee AuthModule

    // UseCase: Sign in with Google
    @Provides
    @Singleton
    fun provideSignInWithGoogleUseCase(
        authRepository: AuthRepository
    ): SignInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository)

    // UseCase: Get current signed-in user
    @Provides
    @Singleton
    fun provideGetSignedInUserUseCase(
        authRepository: AuthRepository
    ): GetSignedInUserUseCase = GetSignedInUserUseCase(authRepository)

    // LocationService
    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context
    ): LocationService = LocationService(context)

    // UseCase: Get current location
    @Provides
    @Singleton
    fun provideGetCurrentLocationUseCase(
        locationService: LocationService
    ): GetCurrentLocationUseCase = GetCurrentLocationUseCase(locationService)

    @Provides
    @Singleton
    fun provideSignOutUseCase(authRepository: AuthRepository): SignOutUseCase {
        return SignOutUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    fun provideGetEventByIdUseCase(repository: EventRepository): GetEventByIdUseCase =
        GetEventByIdUseCase(repository)

    @Provides
    fun provideUpdateEventUseCase(repository: EventRepository): UpdateEventUseCase =
        UpdateEventUseCase(repository)


}
