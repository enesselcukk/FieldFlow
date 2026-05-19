package com.example.presentation.eventlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.EventRecord
import com.example.domain.usecase.event.ObserveAllEventsUseCase
import com.example.domain.usecase.event.UpdateEventNoteUseCase
import com.example.presentation.eventlog.model.EventLogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventLogViewModel @Inject constructor(
    private val observeAllEvents: ObserveAllEventsUseCase,
    private val updateEventNote: UpdateEventNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventLogUiState())
    val uiState: StateFlow<EventLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAllEvents().collect { events ->
                _uiState.update { it.copy(events = events) }
            }
        }
    }

    fun onEventSelected(event: EventRecord) {
        _uiState.update { it.copy(selectedEvent = event, noteInput = event.note) }
    }

    fun onNoteDismiss() {
        _uiState.update { it.copy(selectedEvent = null, noteInput = "") }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(noteInput = note) }
    }

    fun onNoteSave() {
        val event = _uiState.value.selectedEvent ?: return
        val note = _uiState.value.noteInput.trim()
        viewModelScope.launch {
            updateEventNote(event.id, note)
            _uiState.update { it.copy(selectedEvent = null, noteInput = "") }
        }
    }
}
