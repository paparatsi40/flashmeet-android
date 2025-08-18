package com.carlitoswy.flashmeet.data.firebase

import com.carlitoswy.flashmeet.domain.model.UserProfile
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = Firebase.firestore

    suspend fun saveUserProfile(user: UserProfile) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }
}
