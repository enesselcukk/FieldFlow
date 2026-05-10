package com.example.data.repository

import com.example.data.local.dao.EventRecordDao
import com.example.data.local.entity.EventRecordEntity
import com.example.domain.model.EventRecord
import com.example.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val dao: EventRecordDao
) : EventRepository {

    override suspend fun saveEvent(event: EventRecord) {
        dao.insert(event.toEntity())
    }

    override fun observeAll(): Flow<List<EventRecord>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun updateNote(id: Long, note: String) =
        dao.updateNote(id, note)

    override suspend fun getUnsyncedEvents(): List<EventRecord> =
        dao.getUnsynced().map { it.toDomain() }

    override suspend fun markEventsSynced(ids: List<Long>, syncedAt: Long) {
        if (ids.isNotEmpty()) dao.markSynced(ids, syncedAt)
    }

    private fun EventRecord.toEntity() = EventRecordEntity(
        id = id,
        timestamp = timestamp,
        type = type.name,
        detail = detail,
        note = note,
        isSynced = isSynced,
        syncedAt = syncedAt
    )

    private fun EventRecordEntity.toDomain() = EventRecord(
        id = id,
        timestamp = timestamp,
        type = EventRecord.EventType.valueOf(type),
        detail = detail,
        note = note,
        syncedAt = syncedAt
    )
}
