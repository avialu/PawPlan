package com.avialu.pawplan.data.household

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.Household
import com.avialu.pawplan.data.models.HouseholdMember
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class HouseholdRepository {

    private val auth = FirebaseProvider.auth
    private val db = FirebaseProvider.firestore

    private fun generateJoinCode(length: Int = 6): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    suspend fun createHousehold(name: String): String {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        var joinCode: String
        while (true) {
            joinCode = generateJoinCode()
            val exists = db.collection("households")
                .whereEqualTo("joinCode", joinCode)
                .limit(1)
                .get()
                .await()
                .documents
                .isNotEmpty()
            if (!exists) break
        }

        val docRef = db.collection("households").document()
        val householdId = docRef.id

        val household = Household(
            id = householdId,
            name = name.trim(),
            joinCode = joinCode,
            createdAt = System.currentTimeMillis(),
            createdBy = uid
        )

        docRef.set(household).await()

        val member = HouseholdMember(
            uid = uid,
            role = "owner",
            joinedAt = System.currentTimeMillis()
        )
        docRef.collection("members").document(uid).set(member).await()

        setActiveHousehold(uid, householdId)
        return joinCode
    }

    fun observeHousehold(householdId: String): Flow<Household?> = callbackFlow {
        val ref = db.collection("households").document(householdId)

        val listener = ref.addSnapshotListener { snapshot, _ ->
            val household = snapshot?.toObject(Household::class.java)
            trySend(household)
        }

        awaitClose { listener.remove() }
    }

    fun observeUserDisplayName(uid: String): Flow<String?> = callbackFlow {
        val ref = db.collection("users").document(uid)

        val listener = ref.addSnapshotListener { snapshot, _ ->
            val name = snapshot?.getString("displayName")
            trySend(name)
        }

        awaitClose { listener.remove() }
    }

    suspend fun joinHouseholdByCode(code: String): String {
        val uid = auth.currentUser?.uid ?: error("Not logged in")
        val joinCode = code.trim().uppercase()

        val match = db.collection("households")
            .whereEqualTo("joinCode", joinCode)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: error("Invalid join code")

        val householdId = match.id

        val member = HouseholdMember(
            uid = uid,
            role = "member",
            joinedAt = System.currentTimeMillis()
        )

        db.collection("households").document(householdId)
            .collection("members").document(uid)
            .set(member).await()

        setActiveHousehold(uid, householdId)
        return householdId
    }

    private suspend fun setActiveHousehold(uid: String, householdId: String) {
        db.collection("users").document(uid)
            .update("activeHouseholdId", householdId)
            .await()
    }
}