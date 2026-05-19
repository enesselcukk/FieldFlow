package com.example.presentation.eventlog.model

import com.example.domain.model.EventRecord

data class EventLogUiState(
    val events: List<EventRecord> = emptyList(),
    val selectedEvent: EventRecord? = null,
    val noteInput: String = "",
)
