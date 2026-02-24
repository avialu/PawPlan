package com.avialu.pawplan.data.household

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.Household
import com.avialu.pawplan.data.models.HouseholdMember
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class HouseholdRepository {

    private val auth = FirebaseProvider.auth
    private val db = FirebaseProvider.firestore

    private fun generateJoinCode(length: Int = 6): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // בלי 0/O/1/I
        return (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    /**
     * Creates household + owner member + sets users/{uid}.activeHouseholdId
     * Returns joinCode
     */
    suspend fun createHousehold(name: String): String {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        // Find a free join code
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

        // IMPORTANT: return the join code (so we can display it)
        return joinCode
    }

    /**
     * Joins by code + creates member doc + sets users/{uid}.activeHouseholdId
     * Returns householdId
     */
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