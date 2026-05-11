package com.example.domain.usecase.notification

import com.example.domain.fakes.FakeNotificationRepository
import com.example.domain.model.NotificationRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationUseCasesTest {

    private val record = NotificationRecord(type = "t", timestamp = 1L)

    @Test
    fun deleteRoutesId() = runTest {
        val repo = FakeNotificationRepository()
        DeleteNotificationUseCase(repo)(77L)
        assertEquals(77L, repo.lastDeletedId)
    }

    @Test
    fun deleteAllRoutes() = runTest {
        val repo = FakeNotificationRepository()
        DeleteAllNotificationsUseCase(repo)()
        assertTrue(repo.deletedAllInvoked)
    }

    @Test
    fun saveRoutesRecord() = runTest {
        val repo = FakeNotificationRepository()
        SaveNotificationUseCase(repo)(record)
        assertEquals(record, repo.lastSaved)
    }

    @Test
    fun markAllReadRoutes() = runTest {
        val repo = FakeNotificationRepository()
        MarkAllNotificationsReadUseCase(repo)()
        assertTrue(repo.markAllReadInvoked)
    }

    @Test
    fun observeAllExposesRepoFlow() = runTest {
        val repo = FakeNotificationRepository(initialAll = listOf(record))
        val items = ObserveAllNotificationsUseCase(repo)().first()
        assertEquals(listOf(record), items)
    }

    @Test
    fun observeUnreadExposesRepoCount() = runTest {
        val repo = FakeNotificationRepository(initialUnread = 3)
        val count = ObserveUnreadNotificationCountUseCase(repo)().first()
        assertEquals(3, count)
    }

    @Test
    fun defaultUnreadStartsAtZeroWhenUnspecified() = runTest {
        val repo = FakeNotificationRepository()
        val count = ObserveUnreadNotificationCountUseCase(repo)().first()
        assertEquals(0, count)
    }
}
