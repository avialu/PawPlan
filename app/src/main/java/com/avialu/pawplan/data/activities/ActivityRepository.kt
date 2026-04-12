package com.avialu.pawplan.data.activities

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.ActivityType
import com.avialu.pawplan.data.models.PetActivity
import com.avialu.pawplan.ui.util.startOfDay
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
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
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(PetActivity::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    // ✅ היסטוריה של משתמש
    fun observeUserActivities(householdId: String, uid: String): Flow<List<PetActivity>> = callbackFlow {
        val reg = db.collectionGroup("activities")
            .whereEqualTo("householdId", householdId)
            .whereEqualTo("createdBy", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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
        note: String?,
        timestamp: Long
    ) {
        val currentUser = auth.currentUser ?: error("Not logged in")
        val uid = currentUser.uid

        val userSnap = db.collection("users").document(uid).get().await()
        val userName = userSnap.getString("displayName")?.trim()
            ?: currentUser.displayName
            ?: currentUser.email?.substringBefore("@")
            ?: "Unknown"

        val petRef = db.collection("households").document(householdId)
            .collection("pets").document(petId)

        val petSnap = petRef.get().await()
        val petName = petSnap.getString("name") ?: "Pet"

        val activityRef = activitiesRef(householdId, petId).document()

        val activityData = mapOf(
            "id" to activityRef.id,
            "type" to type,
            "note" to note?.trim(),
            "timestamp" to timestamp,
            "createdBy" to uid,
            "createdByName" to userName,
            "householdId" to householdId,
            "petId" to petId,
            "petName" to petName,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val dayStart = startOfDay(timestamp)

        // ---------------- WALK ----------------
        val walkUpdate = if (type == ActivityType.WALK.name) {
            val existingDayStart = petSnap.getLong("walkCountDayStart")
            val existingCount = (petSnap.getLong("walkCountToday") ?: 0L).toInt()

            val newCount =
                if (existingDayStart != null && existingDayStart == dayStart) existingCount + 1
                else 1

            mapOf(
                "lastWalkAt" to timestamp,
                "lastWalkByName" to userName,
                "walkCountDayStart" to dayStart,
                "walkCountToday" to newCount
            )
        } else emptyMap()

        // ---------------- FEED ----------------
        val feedUpdate = if (type == ActivityType.FEED.name) {
            val existingDayStart = petSnap.getLong("feedCountDayStart")
            val existingCount = (petSnap.getLong("feedCountToday") ?: 0L).toInt()

            val newCount =
                if (existingDayStart != null && existingDayStart == dayStart) existingCount + 1
                else 1

            mapOf(
                "lastFeedAt" to timestamp,
                "lastFeedByName" to userName,
                "feedCountDayStart" to dayStart,
                "feedCountToday" to newCount
            )
        } else emptyMap()

        val vaccUpdate = if (type == ActivityType.VACCINATION.name) {
            mapOf("lastVaccinationAt" to timestamp)
        } else emptyMap()

        val groomUpdate = if (type == ActivityType.GROOMING.name) {
            mapOf("lastGroomingAt" to timestamp)
        } else emptyMap()

        val petUpdate = walkUpdate + feedUpdate + vaccUpdate + groomUpdate

        db.batch().apply {
            set(activityRef, activityData)
            if (petUpdate.isNotEmpty()) update(petRef, petUpdate)
        }.commit().await()
    }
}