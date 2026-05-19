package com.example.data.repository

import com.example.data.local.dao.NotificationDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.NotificationRecord
import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override suspend fun save(record: NotificationRecord) {
        dao.insert(record.toEntity())
    }

    override fun observeAll(): Flow<List<NotificationRecord>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeUnreadCount(): Flow<Int> =
        dao.observeUnreadCount()

    override suspend fun markAllRead() =
        dao.markAllRead()

    override suspend fun delete(id: Long) =
        dao.delete(id)

    override suspend fun deleteAll() =
        dao.deleteAll()
}
