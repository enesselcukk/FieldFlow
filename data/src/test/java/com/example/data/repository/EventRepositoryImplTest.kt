package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.db.AppDatabase
import com.example.domain.model.EventRecord
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
class EventRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: EventRepositoryImpl

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EventRepositoryImpl(database.eventRecordDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveObserveRoundTrip() = runBlocking {
        val inserted = EventRecord(
            timestamp = 50L,
            type = EventRecord.EventType.GEOFENCE_ENTER,
            detail = "d",
            note = "n"
        )
        repository.saveEvent(inserted)
        val out = repository.observeAll().first().single()
        assertTrue(out.id > 0L)
        assertEquals(inserted.timestamp, out.timestamp)
        assertEquals(inserted.type, out.type)
        assertEquals(inserted.detail, out.detail)
        assertEquals(inserted.note, out.note)
    }

    @Test
    fun updateNotePersists() = runBlocking {
        repository.saveEvent(
            EventRecord(timestamp = 1L, type = EventRecord.EventType.INTERNET_LOST)
        )
        val id = repository.observeAll().first().first().id
        repository.updateNote(id, "after")
        assertEquals("after", repository.observeAll().first().first().note)
    }

    @Test
    fun getUnsyncedExcludesSynced() = runBlocking {
        repository.saveEvent(
            EventRecord(timestamp = 1L, type = EventRecord.EventType.INTERNET_LOST)
        )
        assertEquals(1, repository.getUnsyncedEvents().size)
        val id = repository.getUnsyncedEvents().first().id
        repository.markEventsSynced(listOf(id), 99L)
        assertTrue(repository.getUnsyncedEvents().isEmpty())
    }

    @Test
    fun markSyncedNoOpWhenIdsEmpty() = runBlocking {
        repository.saveEvent(
            EventRecord(timestamp = 1L, type = EventRecord.EventType.INTERNET_LOST)
        )
        repository.markEventsSynced(emptyList(), 123L)
        assertEquals(1, repository.getUnsyncedEvents().size)
    }
}
