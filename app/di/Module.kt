@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    fun provideAuthRepository(
        authService: AuthService,
        firestore: FirestoreService
    ): AuthRepository = AuthRepositoryImpl(authService, firestore)
}
