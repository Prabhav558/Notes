package com.example.notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.NoteDocument
import com.example.notes.data.NoteItem
import com.example.notes.data.NotesRepository
import com.example.notes.data.Partnership
import com.example.notes.widget.updateAllWidgets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val notesRepo = NotesRepository()

    private val _noteDocuments = MutableStateFlow<List<NoteDocument>>(emptyList())
    val noteDocuments: StateFlow<List<NoteDocument>> = _noteDocuments.asStateFlow()

    private val _tasks = MutableStateFlow<List<NoteItem>>(emptyList())
    val tasks: StateFlow<List<NoteItem>> = _tasks.asStateFlow()

    private var partnershipId: String = ""
    private var currentUserUid: String = ""

    fun initialize(partnershipId: String, currentUserUid: String) {
        if (this.partnershipId == partnershipId) return
        this.partnershipId = partnershipId
        this.currentUserUid = currentUserUid

        viewModelScope.launch {
            notesRepo.getNoteDocumentsFlow(partnershipId).collect { docs ->
                _noteDocuments.value = docs
                updateAllWidgets(getApplication())
            }
        }
        viewModelScope.launch {
            notesRepo.getTasksFlow(partnershipId).collect { tasksList ->
                _tasks.value = tasksList
                updateAllWidgets(getApplication())
            }
        }
    }

    fun createNoteDocument(title: String) {
        if (title.isBlank() || partnershipId.isEmpty()) return
        viewModelScope.launch {
            notesRepo.createNoteDocument(partnershipId, title, currentUserUid)
            updateAllWidgets(getApplication())
        }
    }

    fun deleteNoteDocument(docId: String) {
        if (partnershipId.isEmpty()) return
        viewModelScope.launch {
            notesRepo.deleteNoteDocument(partnershipId, docId)
            updateAllWidgets(getApplication())
        }
    }

    fun addTask(text: String) {
        if (text.isBlank() || partnershipId.isEmpty()) return
        viewModelScope.launch {
            notesRepo.addTask(partnershipId, text, currentUserUid)
            updateAllWidgets(getApplication())
        }
    }

    fun toggleTask(taskId: String, isCompleted: Boolean) {
        if (partnershipId.isEmpty()) return
        viewModelScope.launch {
            notesRepo.toggleTask(partnershipId, taskId, isCompleted)
            updateAllWidgets(getApplication())
        }
    }

    fun deleteTask(taskId: String) {
        if (partnershipId.isEmpty()) return
        viewModelScope.launch {
            notesRepo.deleteTask(partnershipId, taskId)
            updateAllWidgets(getApplication())
        }
    }

    fun getPartnerColor(note: NoteItem, partnership: Partnership?): String {
        if (partnership == null) return "man"
        return if (note.createdBy == partnership.partner1Uid) {
            partnership.partner1Gender
        } else {
            partnership.partner2Gender
        }
    }

    fun getDocPartnerColor(doc: NoteDocument, partnership: Partnership?): String {
        if (partnership == null) return "man"
        return if (doc.createdBy == partnership.partner1Uid) {
            partnership.partner1Gender
        } else {
            partnership.partner2Gender
        }
    }
}
