package com.example.data.mapper

import com.example.data.local.entity.EventRecordEntity
import com.example.domain.model.EventRecord

internal fun EventRecordEntity.toDomain(): EventRecord =
    EventRecord(
        id = id,
        timestamp = timestamp,
        type = EventRecord.EventType.valueOf(type),
        detail = detail,
        note = note,
        syncedAt = syncedAt,
    )

internal fun EventRecord.toEntity(): EventRecordEntity =
    EventRecordEntity(
        id = id,
        timestamp = timestamp,
        type = type.name,
        detail = detail,
        note = note,
        isSynced = isSynced,
        syncedAt = syncedAt,
    )
