package com.avialu.pawplan.data.user

import com.avialu.pawplan.data.firebase.FirebaseProvider
import com.avialu.pawplan.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose

class UserRepository {

    private val auth = FirebaseProvider.auth
    private val db = FirebaseProvider.firestore

    suspend fun ensureUserProfileExists() {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid)

        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            val user = User(
                uid = uid,
                email = auth.currentUser?.email ?: "",
                displayName = auth.currentUser?.email?.substringBefore("@") ?: "",
                createdAt = System.currentTimeMillis()
            )
            docRef.set(user).await()
        }
    }

    fun observeUserProfile(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration =
            db.collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, _ ->
                    trySend(snapshot?.toObject(User::class.java))
                }

        awaitClose { registration.remove() }
    }
}