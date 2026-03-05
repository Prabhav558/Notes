package com.example.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.NoteDocument
import com.example.notes.data.NotesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel : ViewModel() {
    private val notesRepo = NotesRepository()

    private val _document = MutableStateFlow<NoteDocument?>(null)
    val document: StateFlow<NoteDocument?> = _document.asStateFlow()

    private var partnershipId = ""
    private var documentId = ""
    private var saveJob: Job? = null

    fun initialize(partnershipId: String, documentId: String) {
        if (this.documentId == documentId) return
        this.partnershipId = partnershipId
        this.documentId = documentId

        viewModelScope.launch {
            notesRepo.getNoteDocumentFlow(partnershipId, documentId).collect { doc ->
                _document.value = doc
            }
        }
    }

    fun updateTitle(title: String) {
        _document.value = _document.value?.copy(title = title)
        debounceSave()
    }

    fun updateContent(content: String) {
        _document.value = _document.value?.copy(content = content)
        debounceSave()
    }

    private fun debounceSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            val doc = _document.value ?: return@launch
            notesRepo.updateNoteDocument(partnershipId, documentId, doc.title, doc.content)
        }
    }

    fun saveNow() {
        saveJob?.cancel()
        viewModelScope.launch {
            val doc = _document.value ?: return@launch
            notesRepo.updateNoteDocument(partnershipId, documentId, doc.title, doc.content)
        }
    }
}
