package com.carlitoswy.flashmeet.presentation.myevents

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class MyEventUI(
    val id: String,
    val title: String,
    val city: String,
    val dateMillis: Long,
    val promoted: Boolean
)

@HiltViewModel
class MyEventsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow<List<MyEventUI>>(emptyList())
    val state: StateFlow<List<MyEventUI>> = _state

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var listener: ListenerRegistration? = null

    init {
        subscribe()
    }

    /** Suscripción en tiempo real a la colección correcta: flyers */
    private fun subscribe() {
        listener?.remove()
        _loading.value = true

        val uid = auth.currentUser?.uid
        if (uid == null) {
            _state.value = emptyList()
            _loading.value = false
            return
        }

        // 🔁 Ahora escuchamos en tiempo real en flyers del usuario
        val query = db.collection("flyers")
            .whereEqualTo("ownerId", uid)
            .orderBy("dateMillis", Query.Direction.DESCENDING)

        listener = query.addSnapshotListener { snap, err ->
            _loading.value = false
            if (err != null || snap == null) {
                _state.value = emptyList()
                return@addSnapshotListener
            }
            _state.value = snap.documents.mapNotNull { d ->
                val title = d.getString("title") ?: return@mapNotNull null
                val city = d.getString("city") ?: ""
                val date = (d.getLong("dateMillis") ?: d.getLong("timestamp") ?: 0L)
                val promoted = d.getBoolean("promoted") ?: false
                MyEventUI(d.id, title, city, date, promoted)
            }
        }
    }

    /** Si quieres forzar un reintento manual desde UI */
    fun refresh() {
        subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
