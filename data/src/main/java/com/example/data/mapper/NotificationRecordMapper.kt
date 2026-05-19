package com.example.data.mapper

import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.NotificationRecord

internal fun NotificationEntity.toDomain(): NotificationRecord =
    NotificationRecord(
        id = id,
        type = type,
        timestamp = timestamp,
        extraArg = extraArg,
        isRead = isRead,
    )

internal fun NotificationRecord.toEntity(): NotificationEntity =
    NotificationEntity(
        id = id,
        type = type,
        timestamp = timestamp,
        extraArg = extraArg,
        isRead = isRead,
    )
