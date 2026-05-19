package com.example.data.repository

import com.example.data.local.dao.EventRecordDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.EventRecord
import com.example.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EventRepositoryImpl @Inject constructor(
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
}
