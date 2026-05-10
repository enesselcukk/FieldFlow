package com.example.presentation.notification

import com.example.domain.model.NotificationRecord

data class NotificationListUiState(
    val notifications: List<NotificationRecord> = emptyList(),
    val unreadCount: Int = 0,
    val showDeleteAllDialog: Boolean = false
)
