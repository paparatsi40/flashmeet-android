package com.carlitoswy.flashmeet.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(private val activity: Activity) {

    companion object {
        const val RC_SIGN_IN = 1001
    }

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.carlitoswy.flashmeet.R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(activity, gso)
    }

    fun launchSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun getSignInResultFromIntent(data: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            task.await()
        } catch (e: ApiException) {
            null
        }
    }
}
