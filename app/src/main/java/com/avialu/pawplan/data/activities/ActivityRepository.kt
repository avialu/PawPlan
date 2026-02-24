package com.avialu.pawplan.data.activities

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.PetActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ActivityRepository {

    private val auth = FirebaseProvider.auth
    private val db = FirebaseProvider.firestore

    private fun activitiesRef(householdId: String, petId: String) =
        db.collection("households").document(householdId)
            .collection("pets").document(petId)
            .collection("activities")

    fun observeActivities(householdId: String, petId: String): Flow<List<PetActivity>> = callbackFlow {
        val reg = activitiesRef(householdId, petId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(PetActivity::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addActivity(
        householdId: String,
        petId: String,
        type: String,
        note: String?
    ) {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        val petRef = db.collection("households").document(householdId)
            .collection("pets").document(petId)

        val petSnap = petRef.get().await()
        val petName = petSnap.getString("name") ?: ""

        val activityRef = activitiesRef(householdId, petId).document()
        val feedRef = db.collection("households").document(householdId)
            .collection("feed").document()

        val ts = System.currentTimeMillis()
        val cleanNote = note?.trim()?.takeIf { it.isNotBlank() }

        val activityData = mapOf(
            "id" to activityRef.id,
            "type" to type,
            "note" to cleanNote,
            "timestamp" to ts,
            "createdBy" to uid
        )

        val feedData = mapOf(
            "id" to feedRef.id,
            "petId" to petId,
            "petName" to petName,
            "type" to type,
            "note" to cleanNote,
            "timestamp" to ts,
            "createdBy" to uid
        )

        val petUpdate = mapOf(
            "lastActivityType" to type,
            "lastActivityAt" to ts
        )

        db.batch().apply {
            set(activityRef, activityData)
            set(feedRef, feedData)
            update(petRef, petUpdate)
        }.commit().await()
    }
}