package com.avialu.pawplan.data.feed

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.FeedEvent
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FeedRepository {

    private val db = FirebaseProvider.firestore

    private fun feedRef(householdId: String) =
        db.collection("households").document(householdId).collection("feed")

    fun observeFeed(householdId: String): Flow<List<FeedEvent>> = callbackFlow {
        val reg = feedRef(householdId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(FeedEvent::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}