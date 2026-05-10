package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["timestamp"])]
)
internal data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestamp: Long,
    @ColumnInfo(name = "extra_arg") val extraArg: String? = null,
    @ColumnInfo(name = "is_read", defaultValue = "0") val isRead: Boolean = false
)
