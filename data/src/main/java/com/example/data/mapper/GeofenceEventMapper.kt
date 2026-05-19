package com.example.data.mapper

import com.example.data.local.entity.GeofenceEventEntity
import com.example.domain.model.GeofenceEvent

internal fun GeofenceEventEntity.toDomain(): GeofenceEvent =
    GeofenceEvent(
        id = id,
        zoneId = zoneId,
        zoneName = zoneName,
        timestamp = timestamp,
        eventType = GeofenceEvent.EventType.valueOf(eventType),
    )

internal fun GeofenceEvent.toEntity(): GeofenceEventEntity =
    GeofenceEventEntity(
        id = id,
        zoneId = zoneId,
        zoneName = zoneName,
        timestamp = timestamp,
        eventType = eventType.name,
    )
