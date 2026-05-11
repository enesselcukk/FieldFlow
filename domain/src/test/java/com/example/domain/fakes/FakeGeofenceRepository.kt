package com.example.domain.fakes

import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import com.example.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeGeofenceRepository(
    observeZones: List<GeofenceZone> = emptyList(),
    var zonesForGetAll: List<GeofenceZone> = emptyList(),
    private val recentEventsFlow: MutableStateFlow<List<GeofenceEvent>> =
        MutableStateFlow(emptyList())
) : GeofenceRepository {

    private val zonesState = MutableStateFlow(observeZones)

    var lastSavedZone: GeofenceZone? = null
        private set
    var lastDeletedZoneId: Long? = null
        private set
    var lastSavedEvent: GeofenceEvent? = null
        private set
    var lastObserveRecentLimit: Int? = null
        private set


    override suspend fun saveZone(zone: GeofenceZone) {
        lastSavedZone = zone
    }

    override suspend fun deleteZone(zoneId: Long) {
        lastDeletedZoneId = zoneId
    }

    override suspend fun getAllZones(): List<GeofenceZone> = zonesForGetAll

    override fun observeAllZones(): Flow<List<GeofenceZone>> = zonesState.asStateFlow()

    override suspend fun saveEvent(event: GeofenceEvent) {
        lastSavedEvent = event
    }

    override fun observeRecentEvents(limit: Int): Flow<List<GeofenceEvent>> {
        lastObserveRecentLimit = limit
        return recentEventsFlow.asStateFlow()
    }
}
