package com.avialu.pawplan.data.auth

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseProvider.auth
    private val db = FirebaseProvider.firestore

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun signUp(email: String, password: String, displayName: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Missing uid")

        val userDoc = User(
            uid = uid,
            email = email,
            displayName = displayName,
            createdAt = System.currentTimeMillis()
        )

        db.collection("users").document(uid).set(userDoc).await()
        return uid
    }

    suspend fun signIn(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: error("Missing uid")
    }

    fun signOut() {
        auth.signOut()
    }
}