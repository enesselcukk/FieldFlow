package com.example.domain.usecase.geofence

import com.example.domain.fakes.FakeGeofenceRepository
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GeofenceUseCasesTest {

    private val zone = GeofenceZone(
        name = "z",
        centerLat = 1.0,
        centerLng = 2.0,
        radiusMeters = 100.0
    )

    private val geofenceEvent = GeofenceEvent(
        zoneId = 1L,
        zoneName = "z",
        timestamp = 1L,
        eventType = GeofenceEvent.EventType.ENTER
    )

    @Test
    fun saveZoneRoutes() = runTest {
        val repo = FakeGeofenceRepository()
        SaveGeofenceZoneUseCase(repo)(zone)
        assertEquals(zone, repo.lastSavedZone)
    }

    @Test
    fun deleteZoneRoutesId() = runTest {
        val repo = FakeGeofenceRepository()
        DeleteGeofenceZoneUseCase(repo)(42L)
        assertEquals(42L, repo.lastDeletedZoneId)
    }

    @Test
    fun getAllReturnsRepositoryList() = runTest {
        val repo = FakeGeofenceRepository(zonesForGetAll = listOf(zone))
        val out = GetAllGeofenceZonesUseCase(repo)()
        assertEquals(listOf(zone), out)
    }

    @Test
    fun saveEventRoutes() = runTest {
        val repo = FakeGeofenceRepository()
        SaveGeofenceEventUseCase(repo)(geofenceEvent)
        assertEquals(geofenceEvent, repo.lastSavedEvent)
    }

    @Test
    fun observeZonesExposesFlow() = runTest {
        val repo = FakeGeofenceRepository(observeZones = listOf(zone))
        val items = ObserveGeofenceZonesUseCase(repo)().first()
        assertEquals(listOf(zone), items)
    }

    @Test
    fun observeRecentUsesDefaultLimit() = runTest {
        val flow = MutableStateFlow(listOf(geofenceEvent))
        val repo = FakeGeofenceRepository(recentEventsFlow = flow)
        ObserveRecentGeofenceEventsUseCase(repo)().first()
        assertEquals(50, repo.lastObserveRecentLimit)
    }
}
