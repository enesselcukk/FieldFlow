package com.example.domain.model

data class NotificationRecord(
    val id: Long = 0,
    val type: String,
    val timestamp: Long,
    val extraArg: String? = null,
    val isRead: Boolean = false
)
