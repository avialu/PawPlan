package com.avialu.pawplan.data.pets

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.Pet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PetsRepository {

    private val auth = FirebaseProvider.auth
    private val db = FirebaseProvider.firestore

    private fun petsRef(householdId: String) =
        db.collection("households").document(householdId).collection("pets")

    fun observePets(householdId: String): Flow<List<Pet>> = callbackFlow {
        val reg = petsRef(householdId)
            .orderBy("createdAt")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Pet::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addPet(
        householdId: String,
        type: String,
        name: String,
        breed: String,
        birthYear: Int?
    ) {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        val docRef = petsRef(householdId).document()
        val pet = Pet(
            id = docRef.id,
            type = type.trim().lowercase(),
            name = name.trim(),
            breed = breed.trim(),
            birthYear = birthYear,
            createdAt = System.currentTimeMillis(),
            createdBy = uid
        )

        docRef.set(pet).await()
    }

    suspend fun updatePet(
        householdId: String,
        petId: String,
        type: String,
        name: String,
        breed: String,
        birthYear: Int?
    ) {
        val data = mapOf(
            "type" to type.trim().lowercase(),
            "name" to name.trim(),
            "breed" to breed.trim(),
            "birthYear" to birthYear
        )

        petsRef(householdId).document(petId).update(data).await()
    }

    suspend fun getPetById(householdId: String, petId: String): Pet {
        val snap = petsRef(householdId).document(petId).get().await()
        val pet = snap.toObject(Pet::class.java) ?: error("Pet not found")
        return pet.copy(id = snap.id)
    }

    suspend fun deletePet(householdId: String, petId: String) {
        petsRef(householdId).document(petId).delete().await()
    }
}