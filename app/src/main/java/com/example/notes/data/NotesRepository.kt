package com.example.notes.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotesRepository {
    private val db = FirebaseFirestore.getInstance()

    // ── Note Documents ──

    fun getNoteDocumentsFlow(partnershipId: String): Flow<List<NoteDocument>> = callbackFlow {
        val listener: ListenerRegistration = db.collection("partnerships")
            .document(partnershipId)
            .collection("noteDocuments")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    NoteDocument(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        createdBy = data["createdBy"] as? String ?: "",
                        createdAt = data["createdAt"] as? Timestamp,
                        updatedAt = data["updatedAt"] as? Timestamp
                    )
                } ?: emptyList()
                trySend(docs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNoteDocument(partnershipId: String, title: String, createdBy: String): Result<String> {
        return try {
            val doc = hashMapOf(
                "title" to title,
                "content" to "",
                "createdBy" to createdBy,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            val ref = db.collection("partnerships")
                .document(partnershipId)
                .collection("noteDocuments")
                .add(doc)
                .await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNoteDocument(partnershipId: String, docId: String, title: String, content: String): Result<Unit> {
        return try {
            db.collection("partnerships")
                .document(partnershipId)
                .collection("noteDocuments")
                .document(docId)
                .update(
                    mapOf(
                        "title" to title,
                        "content" to content,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNoteDocument(partnershipId: String, docId: String): Result<Unit> {
        return try {
            db.collection("partnerships")
                .document(partnershipId)
                .collection("noteDocuments")
                .document(docId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNoteDocumentFlow(partnershipId: String, docId: String): Flow<NoteDocument?> = callbackFlow {
        val listener = db.collection("partnerships")
            .document(partnershipId)
            .collection("noteDocuments")
            .document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val data = snapshot?.data
                if (data != null) {
                    trySend(
                        NoteDocument(
                            id = snapshot.id,
                            title = data["title"] as? String ?: "",
                            content = data["content"] as? String ?: "",
                            createdBy = data["createdBy"] as? String ?: "",
                            createdAt = data["createdAt"] as? Timestamp,
                            updatedAt = data["updatedAt"] as? Timestamp
                        )
                    )
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    // ── Tasks ──

    fun getTasksFlow(partnershipId: String): Flow<List<NoteItem>> = callbackFlow {
        val listener: ListenerRegistration = db.collection("partnerships")
            .document(partnershipId)
            .collection("tasks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    NoteItem(
                        id = doc.id,
                        text = data["text"] as? String ?: "",
                        isTask = true,
                        isCompleted = data["isCompleted"] as? Boolean ?: false,
                        createdBy = data["createdBy"] as? String ?: "",
                        createdAt = data["createdAt"] as? Timestamp,
                        updatedAt = data["updatedAt"] as? Timestamp
                    )
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addTask(partnershipId: String, text: String, createdBy: String): Result<Unit> {
        return try {
            val task = hashMapOf(
                "text" to text,
                "isCompleted" to false,
                "createdBy" to createdBy,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            db.collection("partnerships")
                .document(partnershipId)
                .collection("tasks")
                .add(task)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleTask(partnershipId: String, taskId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            db.collection("partnerships")
                .document(partnershipId)
                .collection("tasks")
                .document(taskId)
                .update(
                    mapOf(
                        "isCompleted" to isCompleted,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(partnershipId: String, taskId: String): Result<Unit> {
        return try {
            db.collection("partnerships")
                .document(partnershipId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Legacy (keeping for backward compat) ──

    fun getNotesFlow(partnershipId: String): Flow<List<NoteItem>> = callbackFlow {
        val listener: ListenerRegistration = db.collection("partnerships")
            .document(partnershipId)
            .collection("notes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    NoteItem(
                        id = doc.id,
                        text = data["text"] as? String ?: "",
                        isTask = data["isTask"] as? Boolean ?: false,
                        isCompleted = data["isCompleted"] as? Boolean ?: false,
                        createdBy = data["createdBy"] as? String ?: "",
                        createdAt = data["createdAt"] as? Timestamp,
                        updatedAt = data["updatedAt"] as? Timestamp
                    )
                } ?: emptyList()
                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addNote(partnershipId: String, text: String, isTask: Boolean, createdBy: String): Result<Unit> {
        return try {
            val note = hashMapOf(
                "text" to text,
                "isTask" to isTask,
                "isCompleted" to false,
                "createdBy" to createdBy,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            db.collection("partnerships")
                .document(partnershipId)
                .collection("notes")
                .add(note)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleNote(partnershipId: String, noteId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            db.collection("partnerships")
                .document(partnershipId)
                .collection("notes")
                .document(noteId)
                .update(
                    mapOf(
                        "isCompleted" to isCompleted,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(partnershipId: String, noteId: String): Result<Unit> {
        return try {
            db.collection("partnerships")
                .document(partnershipId)
                .collection("notes")
                .document(noteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNoteText(partnershipId: String, noteId: String, text: String): Result<Unit> {
        return try {
            db.collection("partnerships")
                .document(partnershipId)
                .collection("notes")
                .document(noteId)
                .update(
                    mapOf(
                        "text" to text,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
