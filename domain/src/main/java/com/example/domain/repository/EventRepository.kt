package com.example.domain.repository

import com.example.domain.model.EventRecord
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun saveEvent(event: EventRecord)
    fun observeAll(): Flow<List<EventRecord>>
    suspend fun updateNote(id: Long, note: String)
}
