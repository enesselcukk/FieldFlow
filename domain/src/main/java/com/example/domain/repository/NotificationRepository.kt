package com.example.domain.repository

import com.example.domain.model.NotificationRecord
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun save(record: NotificationRecord)
    fun observeAll(): Flow<List<NotificationRecord>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun markAllRead()
    suspend fun delete(id: Long)
    suspend fun deleteAll()
}
