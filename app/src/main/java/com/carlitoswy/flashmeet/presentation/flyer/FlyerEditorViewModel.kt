package com.carlitoswy.flashmeet.presentation.flyer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.Flyer
import com.carlitoswy.flashmeet.utils.await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlyerEditorViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val imageUri = MutableStateFlow<Uri?>(null)
    val bgColor = MutableStateFlow(0xFFFFFFFF.toInt())
    val fontName = MutableStateFlow("SansSerif")

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    fun setImage(uri: Uri) { imageUri.value = uri }

    fun saveFlyer(createdBy: String = "anonymous") {
        viewModelScope.launch {
            try {
                val flyerId = firestore.collection("flyers").document().id
                val downloadUrl = imageUri.value?.let { uri ->
                    val fileRef = storage.child("flyers/$flyerId.jpg")
                    val uploadTask = fileRef.putFile(uri).await()
                    fileRef.downloadUrl.await().toString()
                } ?: ""

                val flyer = Flyer(
                    id = flyerId,
                    title = title.value,
                    description = description.value,
                    imageUrl = downloadUrl,
                    createdBy = createdBy,
                    timestamp = System.currentTimeMillis(),
                    bgColor = bgColor.value,
                    fontName = fontName.value
                )

                firestore.collection("flyers").document(flyerId).set(flyer).await()
                _status.value = "ok"
            } catch (e: Exception) {
                _status.value = "Error: ${e.message}"
            }
        }
    }
}
