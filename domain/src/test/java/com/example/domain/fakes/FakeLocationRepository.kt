package com.example.domain.fakes

import com.example.domain.model.LocationRecord
import com.example.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLocationRepository(
    locationsAfterFlow: MutableStateFlow<List<LocationRecord>> =
        MutableStateFlow(emptyList())
) : LocationRepository {

    private val afterFlowInternal = locationsAfterFlow

    var inserted: MutableList<LocationRecord> = mutableListOf()
        private set
    var lastDeleteOlderThan: Long? = null
        private set
    var lastDeleteSyncedOlderThan: Long? = null
        private set
    var locationsAfterTs: Long? = null
        private set

    var unsynced: List<LocationRecord> = emptyList()

    data class LocationsSynced(val ids: List<Long>, val syncedAt: Long)
    var lastLocationsSynced: LocationsSynced? = null
        private set

    override suspend fun insertLocation(record: LocationRecord) {
        inserted.add(record)
    }

    override suspend fun deleteOlderThan(timestampMs: Long) {
        lastDeleteOlderThan = timestampMs
    }

    override suspend fun deleteSyncedOlderThan(timestampMs: Long) {
        lastDeleteSyncedOlderThan = timestampMs
    }

    override fun getLocationsAfter(timestampMs: Long): Flow<List<LocationRecord>> {
        locationsAfterTs = timestampMs
        return afterFlowInternal.asStateFlow()
    }

    override suspend fun getUnsyncedLocations(): List<LocationRecord> = unsynced

    override suspend fun markLocationsSynced(ids: List<Long>, syncedAt: Long) {
        lastLocationsSynced = LocationsSynced(ids, syncedAt)
    }
}
