package com.carlitoswy.flashmeet.domain.repository

import com.carlitoswy.flashmeet.domain.model.Flyer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FlyerRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val flyersCollection = db.collection("flyers")

    suspend fun getFlyers(): List<Flyer> {
        return flyersCollection
            .orderBy("timestamp")
            .get()
            .await()
            .toObjects(Flyer::class.java)
    }

    suspend fun createFlyer(flyer: Flyer) {
        flyersCollection.document(flyer.id).set(flyer).await()
    }
}
