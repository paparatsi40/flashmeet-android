package com.carlitoswy.flashmeet.utils

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseStorageHelper {

    private val storage: FirebaseStorage = Firebase.storage

    suspend fun uploadImageAndGetUrl(imageUri: Uri): String {
        val fileName = "events/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)

        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
}
