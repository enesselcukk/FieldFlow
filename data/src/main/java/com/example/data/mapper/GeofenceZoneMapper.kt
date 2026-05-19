package com.example.data.mapper

import com.example.data.local.entity.GeofenceZoneEntity
import com.example.domain.model.GeofenceZone

internal fun GeofenceZoneEntity.toDomain(): GeofenceZone =
    GeofenceZone(
        id = id,
        name = name,
        centerLat = centerLat,
        centerLng = centerLng,
        radiusMeters = radiusMeters,
    )

internal fun GeofenceZone.toEntity(): GeofenceZoneEntity =
    GeofenceZoneEntity(
        id = id,
        name = name,
        centerLat = centerLat,
        centerLng = centerLng,
        radiusMeters = radiusMeters,
    )
