package com.example.notes.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String, gender: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            val profile = hashMapOf(
                "uid" to user.uid,
                "name" to name,
                "gender" to gender,
                "email" to email
            )
            db.collection("users").document(user.uid).set(profile).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user!!
            // Create profile if it doesn't exist
            val existingProfile = getUserProfile(user.uid)
            if (existingProfile == null) {
                val profile = hashMapOf(
                    "uid" to user.uid,
                    "name" to (user.displayName ?: ""),
                    "gender" to "",
                    "email" to (user.email ?: "")
                )
                db.collection("users").document(user.uid).set(profile).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, name: String, gender: String): Result<Unit> {
        return try {
            db.collection("users").document(uid).update(
                mapOf("name" to name, "gender" to gender)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.data
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
