package com.example.presentation.map

import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import com.example.domain.model.LocationRecord
import com.example.domain.usecase.geofence.DeleteGeofenceZoneUseCase
import com.example.domain.usecase.geofence.ObserveGeofenceZonesUseCase
import com.example.domain.usecase.geofence.ObserveRecentGeofenceEventsUseCase
import com.example.domain.usecase.geofence.SaveGeofenceZoneUseCase
import com.example.domain.usecase.location.ObserveRecentLocationsUseCase
import com.example.presentation.fakes.MutableTrackingRepository
import com.example.presentation.fakes.StubGeofenceRepository
import com.example.presentation.fakes.StubLocationFlowRepository
import com.example.presentation.test.MainDispatcherRule
import com.example.presentation.test.tapState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun harness(
        locations: MutableStateFlow<List<LocationRecord>>,
        zones: MutableStateFlow<List<GeofenceZone>>,
        events: MutableStateFlow<List<GeofenceEvent>>,
        tracking: MutableTrackingRepository = MutableTrackingRepository(),
    ): Pair<MapViewModel, StubGeofenceRepository> {
        val geo = StubGeofenceRepository(zones, events)
        val vm = MapViewModel(
            ObserveRecentLocationsUseCase(StubLocationFlowRepository(locations)),
            ObserveGeofenceZonesUseCase(geo),
            ObserveRecentGeofenceEventsUseCase(geo),
            SaveGeofenceZoneUseCase(geo),
            DeleteGeofenceZoneUseCase(geo),
            tracking,
        )
        return vm to geo
    }

    @Test
    fun aggregatesLocationsAndZones() = runTest {
        val locs = MutableStateFlow(
            listOf(LocationRecord(latitude = 10.0, longitude = 20.0, timestamp = 1L)),
        )
        val zones = MutableStateFlow(
            listOf(
                GeofenceZone(id = 1L, name = "home", centerLat = 10.0, centerLng = 20.0, radiusMeters = 50.0),
            ),
        )
        val (vm, _) = harness(locs, zones, MutableStateFlow(emptyList()))
        tapState(vm.uiState)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.totalTrackCount)
        assertEquals(1, vm.uiState.value.geofenceZones.size)
    }

    @Test
    fun toggleTrackingMirrorsRepository() = runTest {
        val (vm, _) =
            harness(MutableStateFlow(emptyList()), MutableStateFlow(emptyList()), MutableStateFlow(emptyList()))
        tapState(vm.uiState)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isTracking)
        vm.toggleTracking()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isTracking)
    }

    @Test
    fun saveZoneClosesDialog() = runTest {
        val (vm, geo) =
            harness(MutableStateFlow(emptyList()), MutableStateFlow(emptyList()), MutableStateFlow(emptyList()))
        vm.onAddZoneClick()
        assertTrue(vm.showAddZoneDialog.value)
        vm.saveZone("x", 1.0, 2.0, 3.0)
        advanceUntilIdle()
        assertFalse(vm.showAddZoneDialog.value)
        assertEquals("x", geo.lastSavedZone?.name)
    }

    @Test
    fun deleteZoneDelegatesId() = runTest {
        val zones = MutableStateFlow(
            listOf(GeofenceZone(id = 7L, name = "zone", centerLat = 1.0, centerLng = 1.0, radiusMeters = 2.0)),
        )
        val (vm, geo) =
            harness(MutableStateFlow(emptyList()), zones, MutableStateFlow(emptyList()))
        advanceUntilIdle()
        vm.deleteZone(7L)
        advanceUntilIdle()
        assertEquals(7L, geo.lastDeletedZoneId)
    }
}
