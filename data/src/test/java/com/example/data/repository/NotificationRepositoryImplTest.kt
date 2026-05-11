package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.db.AppDatabase
import com.example.domain.model.NotificationRecord
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
class NotificationRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: NotificationRepositoryImpl

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NotificationRepositoryImpl(database.notificationDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveObserveRoundTrip() = runBlocking {
        val inserted = NotificationRecord(type = "x", timestamp = 10L)
        repository.save(inserted)
        val out = repository.observeAll().first().single()
        assertTrue(out.id > 0L)
        assertEquals(inserted.type, out.type)
        assertEquals(inserted.timestamp, out.timestamp)
        assertEquals(inserted.isRead, out.isRead)
        assertEquals(inserted.extraArg, out.extraArg)
    }

    @Test
    fun unreadCountReflectsReadFlag() = runBlocking {
        repository.save(NotificationRecord(type = "a", timestamp = 1L, isRead = false))
        assertEquals(1, repository.observeUnreadCount().first())
        repository.markAllRead()
        assertEquals(0, repository.observeUnreadCount().first())
    }

    @Test
    fun deleteByIdRemovesRow() = runBlocking {
        repository.save(NotificationRecord(type = "b", timestamp = 2L))
        val id = repository.observeAll().first().first().id
        repository.delete(id)
        assertTrue(repository.observeAll().first().none { it.id == id })
    }

    @Test
    fun deleteAllClears() = runBlocking {
        repository.save(NotificationRecord(type = "c", timestamp = 3L))
        repository.deleteAll()
        assertTrue(repository.observeAll().first().isEmpty())
    }
}
