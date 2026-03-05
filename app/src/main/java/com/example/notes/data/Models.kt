package com.example.notes.data

import com.google.firebase.Timestamp

data class Partnership(
    val id: String = "",
    val partner1Uid: String = "",
    val partner2Uid: String = "",
    val partner1Name: String = "",
    val partner2Name: String = "",
    val partner1Gender: String = "",
    val partner2Gender: String = "",
    val pairingCode: String = "",
    val createdAt: Timestamp? = null
)

data class NoteDocument(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class NoteItem(
    val id: String = "",
    val text: String = "",
    val isTask: Boolean = false,
    val isCompleted: Boolean = false,
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
