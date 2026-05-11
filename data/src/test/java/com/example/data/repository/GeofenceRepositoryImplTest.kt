package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.db.AppDatabase
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeofenceRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: GeofenceRepositoryImpl

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GeofenceRepositoryImpl(
            database.geofenceZoneDao(),
            database.geofenceEventDao()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveZoneGetAllRoundTrip() = runBlocking {
        val zone = GeofenceZone(name = "a", centerLat = 1.0, centerLng = 2.0, radiusMeters = 10.0)
        repository.saveZone(zone)
        val out = repository.getAllZones().single()
        assertTrue(out.id > 0L)
        assertEquals(zone.name, out.name)
        assertEquals(zone.centerLat, out.centerLat, 0.0)
        assertEquals(zone.centerLng, out.centerLng, 0.0)
        assertEquals(zone.radiusMeters, out.radiusMeters, 0.0)
    }

    @Test
    fun observeZonesReflectsInserts() = runBlocking {
        repository.saveZone(GeofenceZone(name = "z1", centerLat = 0.0, centerLng = 0.0, radiusMeters = 1.0))
        assertEquals(1, repository.observeAllZones().first().size)
    }

    @Test
    fun deleteZoneRemoves() = runBlocking {
        repository.saveZone(GeofenceZone(name = "rm", centerLat = 0.0, centerLng = 0.0, radiusMeters = 1.0))
        val id = repository.getAllZones().first().id
        repository.deleteZone(id)
        assertTrue(repository.getAllZones().isEmpty())
    }

    @Test
    fun saveEventObserveRecentOrdersAndLimits() = runBlocking {
        repository.saveEvent(
            GeofenceEvent(zoneId = 1L, zoneName = "z", timestamp = 100L, eventType = GeofenceEvent.EventType.ENTER)
        )
        repository.saveEvent(
            GeofenceEvent(zoneId = 1L, zoneName = "z", timestamp = 300L, eventType = GeofenceEvent.EventType.EXIT)
        )
        repository.saveEvent(
            GeofenceEvent(zoneId = 1L, zoneName = "z", timestamp = 200L, eventType = GeofenceEvent.EventType.ENTER)
        )
        val recent = repository.observeRecentEvents(2).first()
        assertEquals(2, recent.size)
        assertEquals(300L, recent[0].timestamp)
        assertEquals(200L, recent[1].timestamp)
        assertEquals(GeofenceEvent.EventType.EXIT, recent[0].eventType)
    }
}
