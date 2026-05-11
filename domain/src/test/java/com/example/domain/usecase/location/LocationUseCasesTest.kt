package com.example.domain.usecase.location

import com.example.domain.constants.DAY_MS
import com.example.domain.constants.SYNCED_LOCATION_RETENTION_MS
import com.example.domain.constants.UNSYNCED_LOCATION_RETENTION_MS
import com.example.domain.fakes.FakeLocationRepository
import com.example.domain.model.LocationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationUseCasesTest {

    private val record = LocationRecord(
        latitude = 1.0,
        longitude = 2.0,
        timestamp = 500L
    )

    @Test
    fun saveInsertsThenAppliesRetention() = runTest {
        val repo = FakeLocationRepository()
        val before = System.currentTimeMillis()
        SaveLocationUseCase(repo)(record)
        val after = System.currentTimeMillis()
        assertEquals(listOf(record), repo.inserted)
        val syncedCutoff = repo.lastDeleteSyncedOlderThan!!
        assertTrue(syncedCutoff in (before - SYNCED_LOCATION_RETENTION_MS)..(after - SYNCED_LOCATION_RETENTION_MS))
        val unsyncedCutoff = repo.lastDeleteOlderThan!!
        assertTrue(unsyncedCutoff in (before - UNSYNCED_LOCATION_RETENTION_MS)..(after - UNSYNCED_LOCATION_RETENTION_MS))
    }

    @Test
    fun observeRecentDelegatesWithDayWindow() = runTest {
        val flowState = MutableStateFlow<List<LocationRecord>>(emptyList())
        val repo = FakeLocationRepository(locationsAfterFlow = flowState)
        val before = System.currentTimeMillis()
        ObserveRecentLocationsUseCase(repo)().first()
        val after = System.currentTimeMillis()
        val ts = repo.locationsAfterTs!!
        assertTrue(ts in (before - DAY_MS)..(after - DAY_MS))
    }
}
