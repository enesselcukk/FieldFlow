package com.example.domain.fakes

import com.example.domain.model.EventRecord
import com.example.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeEventRepository(
    initialAll: List<EventRecord> = emptyList(),
    var unsynced: List<EventRecord> = emptyList()
) : EventRepository {

    private val allState = MutableStateFlow(initialAll)

    var lastSavedEvent: EventRecord? = null
        private set
    var lastUpdateNoteId: Long? = null
        private set
    var lastUpdateNoteContent: String? = null
        private set

    data class SyncMark(val ids: List<Long>, val syncedAt: Long)
    var lastSyncMark: SyncMark? = null
        private set

    override suspend fun saveEvent(event: EventRecord) {
        lastSavedEvent = event
    }

    override fun observeAll(): Flow<List<EventRecord>> = allState.asStateFlow()

    override suspend fun updateNote(id: Long, note: String) {
        lastUpdateNoteId = id
        lastUpdateNoteContent = note
    }

    override suspend fun getUnsyncedEvents(): List<EventRecord> = unsynced

    override suspend fun markEventsSynced(ids: List<Long>, syncedAt: Long) {
        lastSyncMark = SyncMark(ids, syncedAt)
    }
}
