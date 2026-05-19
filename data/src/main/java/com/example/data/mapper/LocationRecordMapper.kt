package com.example.data.mapper

import com.example.data.local.entity.LocationEntity
import com.example.domain.model.LocationRecord

internal fun LocationEntity.toDomain(): LocationRecord =
    LocationRecord(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        syncedAt = syncedAt,
    )

internal fun LocationRecord.toEntity(): LocationEntity =
    LocationEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        isSynced = isSynced,
        syncedAt = syncedAt,
    )
