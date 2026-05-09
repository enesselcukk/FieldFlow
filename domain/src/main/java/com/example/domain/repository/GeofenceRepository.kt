package com.example.domain.repository

import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import kotlinx.coroutines.flow.Flow

interface GeofenceRepository {
    suspend fun saveZone(zone: GeofenceZone)
    suspend fun deleteZone(zoneId: Long)
    suspend fun getAllZones(): List<GeofenceZone>
    fun observeAllZones(): Flow<List<GeofenceZone>>
    suspend fun saveEvent(event: GeofenceEvent)
    fun observeRecentEvents(limit: Int = 50): Flow<List<GeofenceEvent>>
}
