package com.carlitoswy.flashmeet.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.UserProfile // Make sure this import is correct for your UserProfile class
import com.carlitoswy.flashmeet.domain.usecase.SignInWithGoogleUseCase // Make sure this import is correct
import com.carlitoswy.flashmeet.domain.usecase.SignOutUseCase // Make sure this import is correct
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth to check current user
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Your LoginState sealed class definition (Assuming this is in LoginState.kt or similar)
// package com.carlitoswy.flashmeet.presentation.login
/*
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserProfile) : LoginState()
    data class Error(val message: String) : LoginState()
}
*/

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val firebaseAuth: FirebaseAuth // Inject FirebaseAuth to access current user
) : ViewModel() {

    // This MutableStateFlow will hold the current LoginState.
    // We initialize it to Loading so the Splash screen can show a progress indicator
    // while we check the initial authentication state.
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow() // Expose as public StateFlow

    init {
        // Immediately check the authentication state when the ViewModel is created.
        // This determines if the user is already logged in when the app starts.
        checkAuthState()
    }

    /**
     * Checks the current Firebase Authentication state and updates the loginState.
     * This is typically called in the ViewModel's init block.
     */
    private fun checkAuthState() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // User is logged in. Map FirebaseUser to your UserProfile.
            // We need to provide values for ALL properties in UserProfile that don't have defaults
            // FIX: Provide values for 'id' and 'name' which didn't have defaults in your UserProfile definition
            val userProfile = UserProfile(
                uid = currentUser.uid,
                id = currentUser.uid, // FIX: Provide value for 'id', often the same as UID
                email = currentUser.email, // email is String? in FirebaseUser, ok if UserProfile has String?
                displayName = currentUser.displayName, // displayName is String? in FirebaseUser, ok if UserProfile has String?
                photoUrl = currentUser.photoUrl?.toString(), // photoUrl is Uri?, toString gives String?, ok if UserProfile has String?
                // Provide values for interests and reputation (using defaults from UserProfile definition)
                // interests = emptyList(), // Default provided by data class
                // reputation = 0, // Default provided by data class
                // FIX: Provide value for 'name'. Use displayName, with email or "User" as fallback if nullable
                name = currentUser.displayName ?: currentUser.email ?: "User" // Ensure 'name' is provided
            )
            _loginState.value = LoginState.Success(userProfile)
        } else {
            // No user is logged in
            _loginState.value = LoginState.Idle // Transition to Idle if not logged in
        }
    }


    /**
     * Initiates the sign-in process with Google using the provided ID token.
     * Updates the loginState based on the result.
     *
     * @param idToken The ID token received from Google Sign-In.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading // Indicate loading state before the operation

            // Execute the sign-in use case. Assuming this returns Result<Unit> based on your previous code.
            // FIX: Adjusted logic assuming use case only performs auth, doesn't return UserProfile
            val signInResult = signInWithGoogleUseCase(idToken)

            // Update the state based on the result of the use case
            _loginState.value = if (signInResult.isSuccess) {
                // Use case succeeded (user should now be signed in with Firebase Auth)
                val firebaseUser = firebaseAuth.currentUser // <-- Get the newly signed-in user!
                if (firebaseUser != null) {
                    // Map the FirebaseUser to your UserProfile
                    // FIX: Ensure all UserProfile properties that don't have defaults are provided values
                    val userProfile = UserProfile(
                        uid = firebaseUser.uid,
                        id = firebaseUser.uid, // Often use UID as internal ID
                        email = firebaseUser.email, // Use nullable email from FirebaseUser
                        displayName = firebaseUser.displayName, // Use nullable displayName from FirebaseUser
                        photoUrl = firebaseUser.photoUrl?.toString(), // Use nullable photoUrl string from FirebaseUser
                        // interests and reputation use defaults from UserProfile definition
                        // FIX: Provide value for 'name' using displayName/email with fallback
                        name = firebaseUser.displayName ?: firebaseUser.email ?: "User"
                    )
                    LoginState.Success(userProfile) // Set state to Success, passing the created UserProfile
                } else {
                    // This case indicates a problem: use case succeeded but currentUser is null
                    LoginState.Error("Sign-in succeeded but user is null.")
                }
            } else {
                // On failure, get the exception and set the state to Error
                val errorMessage = signInResult.exceptionOrNull()?.localizedMessage ?: "Unknown login error"
                LoginState.Error(errorMessage)
            }
        }
    }

    /**
     * Initiates the sign-out process.
     * Updates the loginState to Idle after signing out.
     *
     * @param onSignedOut Optional callback to execute after successful sign out.
     */
    fun signOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch {
            signOutUseCase()
            // After signing out, set the state back to Idle (or Unauthenticated, depending on your sealed class name)
            _loginState.value = LoginState.Idle
            onSignedOut() // Execute the callback
        }
    }

    /**
     * Resets the login state, typically after an error has been displayed.
     * Sets the state back to Idle.
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Idle // Or LoginState.Error(""), depending on desired behavior
    }
}
