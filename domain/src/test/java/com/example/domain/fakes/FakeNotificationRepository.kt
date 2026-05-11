package com.example.domain.fakes

import com.example.domain.model.NotificationRecord
import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNotificationRepository(
    initialAll: List<NotificationRecord> = emptyList(),
    initialUnread: Int = 0
) : NotificationRepository {

    private val allState = MutableStateFlow(initialAll)
    private val unreadState = MutableStateFlow(initialUnread)

    var lastSaved: NotificationRecord? = null
        private set
    var lastDeletedId: Long? = null
        private set
    var deletedAllInvoked: Boolean = false
        private set
    var markAllReadInvoked: Boolean = false
        private set

    override suspend fun save(record: NotificationRecord) {
        lastSaved = record
    }

    override fun observeAll(): Flow<List<NotificationRecord>> = allState.asStateFlow()

    override fun observeUnreadCount(): Flow<Int> = unreadState.asStateFlow()

    override suspend fun markAllRead() {
        markAllReadInvoked = true
    }

    override suspend fun delete(id: Long) {
        lastDeletedId = id
    }

    override suspend fun deleteAll() {
        deletedAllInvoked = true
    }
}
