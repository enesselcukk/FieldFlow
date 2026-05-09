package com.example.data.repository

import com.example.data.local.dao.GeofenceEventDao
import com.example.data.local.dao.GeofenceZoneDao
import com.example.data.local.entity.GeofenceEventEntity
import com.example.data.local.entity.GeofenceZoneEntity
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import com.example.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRepositoryImpl @Inject constructor(
    private val zoneDao: GeofenceZoneDao,
    private val eventDao: GeofenceEventDao
) : GeofenceRepository {

    override suspend fun saveZone(zone: GeofenceZone) =
        zoneDao.insert(zone.toEntity())

    override suspend fun deleteZone(zoneId: Long) =
        zoneDao.deleteById(zoneId)

    override suspend fun getAllZones(): List<GeofenceZone> =
        zoneDao.getAll().map { it.toDomain() }

    override fun observeAllZones(): Flow<List<GeofenceZone>> =
        zoneDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveEvent(event: GeofenceEvent) =
        eventDao.insert(event.toEntity())

    override fun observeRecentEvents(limit: Int): Flow<List<GeofenceEvent>> =
        eventDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    

    private fun GeofenceZone.toEntity() = GeofenceZoneEntity(
        id = id, name = name,
        centerLat = centerLat, centerLng = centerLng,
        radiusMeters = radiusMeters
    )

    private fun GeofenceZoneEntity.toDomain() = GeofenceZone(
        id = id, name = name,
        centerLat = centerLat, centerLng = centerLng,
        radiusMeters = radiusMeters
    )

    private fun GeofenceEvent.toEntity() = GeofenceEventEntity(
        id = id, zoneId = zoneId, zoneName = zoneName,
        timestamp = timestamp, eventType = eventType.name
    )

    private fun GeofenceEventEntity.toDomain() = GeofenceEvent(
        id = id, zoneId = zoneId, zoneName = zoneName,
        timestamp = timestamp,
        eventType = GeofenceEvent.EventType.valueOf(eventType)
    )
}
