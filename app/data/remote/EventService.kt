package com.carlitoswy.flashmeet.data.remote

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EventService {
    val eventsCollection: CollectionReference = Firebase.firestore.collection("events")
}
