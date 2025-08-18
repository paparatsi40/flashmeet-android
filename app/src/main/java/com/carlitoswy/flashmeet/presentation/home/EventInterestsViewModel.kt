package com.carlitoswy.flashmeet.presentation.home

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EventInterestsViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var listener: ListenerRegistration? = null

    private val _interestedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val interestedEventIds: StateFlow<Set<String>> = _interestedEventIds

    init {
        // Suscríbete si ya hay usuario logueado
        subscribeToUserInterests()
        // Re-suscríbete si cambia el usuario
        auth.addAuthStateListener {
            subscribeToUserInterests()
        }
    }

    private fun subscribeToUserInterests() {
        val uid = auth.currentUser?.uid
        // Limpia anterior
        listener?.remove()
        _interestedEventIds.value = emptySet()

        if (uid == null) return

        listener = db.collection("users")
            .document(uid)
            .collection("interests")
            .addSnapshotListener { snap, _ ->
                val ids = snap?.documents?.map { it.id }?.toSet().orEmpty()
                _interestedEventIds.value = ids
            }
    }

    fun refreshIfNeeded() {
        // Si por alguna razón no hay listener y hay usuario, vuelve a suscribirte
        val uid = auth.currentUser?.uid ?: return
        if (listener == null) subscribeToUserInterests()
    }

    /**
     * Alterna "me interesa" para un evento.
     * Se llama como: scope.launch { interestsVM.toggleInterest(eventId) }
     */
    suspend fun toggleInterest(eventId: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("users")
            .document(uid)
            .collection("interests")
            .document(eventId)

        val exists = ref.get().await().exists()
        if (exists) {
            ref.delete().await()
        } else {
            ref.set(
                mapOf(
                    "eventId" to eventId,
                    "createdAt" to Instant.now().toEpochMilli()
                )
            ).await()
        }
        // No hace falta actualizar _interestedEventIds aquí:
        // el snapshotListener se encargará de reflejar el cambio.
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        listener = null
    }
}
