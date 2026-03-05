package com.example.notes.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PartnershipRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun createPartnership(uid: String, name: String, gender: String): Result<String> {
        return try {
            val code = (100000..999999).random().toString()
            val partnership = hashMapOf(
                "partner1Uid" to uid,
                "partner1Name" to name,
                "partner1Gender" to gender,
                "partner2Uid" to "",
                "partner2Name" to "",
                "partner2Gender" to "",
                "pairingCode" to code,
                "createdAt" to Timestamp.now()
            )
            val docRef = db.collection("partnerships").add(partnership).await()
            // Store partnership reference on user
            db.collection("users").document(uid)
                .update("partnershipId", docRef.id).await()
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinPartnership(uid: String, name: String, gender: String, code: String): Result<String> {
        return try {
            val query = db.collection("partnerships")
                .whereEqualTo("pairingCode", code)
                .whereEqualTo("partner2Uid", "")
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("Invalid code or already used"))
            }

            val doc = query.documents.first()
            db.collection("partnerships").document(doc.id).update(
                mapOf(
                    "partner2Uid" to uid,
                    "partner2Name" to name,
                    "partner2Gender" to gender,
                    "pairingCode" to "" // Clear code after use
                )
            ).await()

            // Store partnership reference on user
            db.collection("users").document(uid)
                .update("partnershipId", doc.id).await()

            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPartnershipForUser(uid: String): Partnership? {
        return try {
            // Check user profile for stored partnership ID
            val userDoc = db.collection("users").document(uid).get().await()
            val partnershipId = userDoc.getString("partnershipId")

            if (partnershipId != null) {
                val doc = db.collection("partnerships").document(partnershipId).get().await()
                if (doc.exists()) {
                    return doc.toObject(Partnership::class.java)?.copy(id = doc.id)
                }
            }

            // Fallback: query by partner fields
            val q1 = db.collection("partnerships")
                .whereEqualTo("partner1Uid", uid).get().await()
            if (!q1.isEmpty) {
                val doc = q1.documents.first()
                return doc.toObject(Partnership::class.java)?.copy(id = doc.id)
            }

            val q2 = db.collection("partnerships")
                .whereEqualTo("partner2Uid", uid).get().await()
            if (!q2.isEmpty) {
                val doc = q2.documents.first()
                return doc.toObject(Partnership::class.java)?.copy(id = doc.id)
            }

            null
        } catch (e: Exception) {
            null
        }
    }
}
