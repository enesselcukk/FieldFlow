package com.example.domain.repository

import com.example.domain.model.LocationRecord
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun insertLocation(record: LocationRecord)
    suspend fun deleteOlderThan(timestampMs: Long)
    suspend fun deleteSyncedOlderThan(timestampMs: Long)
    fun getLocationsAfter(timestampMs: Long): Flow<List<LocationRecord>>
    suspend fun getUnsyncedLocations(): List<LocationRecord>
    suspend fun markLocationsSynced(ids: List<Long>, syncedAt: Long)
}
