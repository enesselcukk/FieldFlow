package com.example.data.repository

import com.example.data.local.dao.LocationDao
import com.example.data.local.entity.LocationEntity
import com.example.domain.model.LocationRecord
import com.example.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao
) : LocationRepository {

    override suspend fun insertLocation(record: LocationRecord) {
        locationDao.insert(record.toEntity())
    }

    override suspend fun deleteOlderThan(timestampMs: Long) {
        locationDao.deleteOlderThan(timestampMs)
    }

    override suspend fun deleteSyncedOlderThan(timestampMs: Long) {
        locationDao.deleteSyncedOlderThan(timestampMs)
    }

    override fun getLocationsAfter(timestampMs: Long): Flow<List<LocationRecord>> =
        locationDao.getLocationsAfter(timestampMs).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getUnsyncedLocations(): List<LocationRecord> =
        locationDao.getUnsynced().map { it.toDomain() }

    override suspend fun markLocationsSynced(ids: List<Long>, syncedAt: Long) {
        if (ids.isNotEmpty()) locationDao.markSynced(ids, syncedAt)
    }

    private fun LocationRecord.toEntity() = LocationEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        isSynced = isSynced,
        syncedAt = syncedAt
    )

    private fun LocationEntity.toDomain() = LocationRecord(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        syncedAt = syncedAt
    )
}
