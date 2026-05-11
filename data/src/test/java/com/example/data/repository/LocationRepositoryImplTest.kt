package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.db.AppDatabase
import com.example.domain.model.LocationRecord
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
class LocationRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LocationRepositoryImpl(database.locationDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getLocationsAfterFiltersByTimestamp() = runBlocking {
        repository.insertLocation(LocationRecord(latitude = 0.0, longitude = 0.0, timestamp = 10L))
        repository.insertLocation(LocationRecord(latitude = 1.0, longitude = 1.0, timestamp = 100L))
        val rows = repository.getLocationsAfter(50L).first()
        assertEquals(1, rows.size)
        assertEquals(100L, rows.first().timestamp)
    }

    @Test
    fun deleteOlderThanRemovesByTimestamp() = runBlocking {
        repository.insertLocation(LocationRecord(latitude = 0.0, longitude = 0.0, timestamp = 5L))
        repository.insertLocation(LocationRecord(latitude = 0.0, longitude = 0.0, timestamp = 500L))
        repository.deleteOlderThan(100L)
        val rows = repository.getLocationsAfter(0L).first()
        assertEquals(1, rows.size)
        assertEquals(500L, rows.first().timestamp)
    }

    @Test
    fun deleteSyncedOlderThanOnlySyncedRows() = runBlocking {
        repository.insertLocation(
            LocationRecord(
                latitude = 0.0,
                longitude = 0.0,
                timestamp = 10L,
                syncedAt = 20L
            )
        )
        repository.insertLocation(
            LocationRecord(latitude = 0.0, longitude = 0.0, timestamp = 15L)
        )
        repository.deleteSyncedOlderThan(100L)
        val rows = repository.getLocationsAfter(0L).first()
        assertEquals(1, rows.size)
        assertEquals(15L, rows.first().timestamp)
    }

    @Test
    fun getUnsyncedAndMarkSynced() = runBlocking {
        repository.insertLocation(LocationRecord(latitude = 0.0, longitude = 0.0, timestamp = 1L))
        assertEquals(1, repository.getUnsyncedLocations().size)
        val id = repository.getUnsyncedLocations().first().id
        repository.markLocationsSynced(listOf(id), 200L)
        assertTrue(repository.getUnsyncedLocations().isEmpty())
        val stored = repository.getLocationsAfter(0L).first().first()
        assertEquals(200L, stored.syncedAt)
    }

    @Test
    fun markLocationsSyncedNoOpWhenIdsEmpty() = runBlocking {
        repository.insertLocation(LocationRecord(latitude = 0.0, longitude = 0.0, timestamp = 1L))
        repository.markLocationsSynced(emptyList(), 999L)
        assertEquals(1, repository.getUnsyncedLocations().size)
    }
}
