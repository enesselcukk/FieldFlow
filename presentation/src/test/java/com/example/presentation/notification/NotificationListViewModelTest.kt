package com.example.presentation.notification

import com.example.domain.model.NotificationRecord
import com.example.domain.usecase.notification.DeleteAllNotificationsUseCase
import com.example.domain.usecase.notification.DeleteNotificationUseCase
import com.example.domain.usecase.notification.MarkAllNotificationsReadUseCase
import com.example.domain.usecase.notification.ObserveAllNotificationsUseCase
import com.example.domain.usecase.notification.ObserveUnreadNotificationCountUseCase
import com.example.presentation.fakes.StubNotificationRepository
import com.example.presentation.test.MainDispatcherRule
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
class NotificationListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun vmFor(
        notifications: MutableStateFlow<List<NotificationRecord>>,
        unread: MutableStateFlow<Int>,
    ): NotificationListViewModel {
        val repo = StubNotificationRepository(notifications, unread)
        return NotificationListViewModel(
            ObserveAllNotificationsUseCase(repo),
            ObserveUnreadNotificationCountUseCase(repo),
            MarkAllNotificationsReadUseCase(repo),
            DeleteNotificationUseCase(repo),
            DeleteAllNotificationsUseCase(repo),
        )
    }

    @Test
    fun combineListsAndUnreadCount() = runTest {
        val notifications =
            MutableStateFlow(
                listOf(NotificationRecord(id = 1L, type = "t", timestamp = 10L, isRead = false)),
            )
        val unread = MutableStateFlow(1)
        val vm = vmFor(notifications, unread)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.unreadCount)
        assertEquals(1, vm.uiState.value.notifications.size)
    }

    @Test
    fun markAllReadReducesUnread() = runTest {
        val notifications =
            MutableStateFlow(
                listOf(NotificationRecord(id = 1L, type = "t", timestamp = 1L, isRead = false)),
            )
        val unread = MutableStateFlow(1)
        val vm = vmFor(notifications, unread)
        advanceUntilIdle()
        vm.markAllAsRead()
        advanceUntilIdle()
        assertEquals(0, vm.uiState.value.unreadCount)
        assertTrue(vm.uiState.value.notifications.all { it.isRead })
    }

    @Test
    fun deleteNotificationUpdatesList() = runTest {
        val notifications =
            MutableStateFlow(
                listOf(NotificationRecord(id = 99L, type = "t", timestamp = 1L)),
            )
        val unread = MutableStateFlow(0)
        val vm = vmFor(notifications, unread)
        advanceUntilIdle()
        vm.onDeleteNotification(99L)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.notifications.isEmpty())
    }

    @Test
    fun deleteAllDialogFlows() {
        val empty = MutableStateFlow(emptyList<NotificationRecord>())
        val unread = MutableStateFlow(0)
        val vm = vmFor(empty, unread)
        vm.onDeleteAllClick()
        assertTrue(vm.uiState.value.showDeleteAllDialog)
        vm.onDeleteAllDismiss()
        assertFalse(vm.uiState.value.showDeleteAllDialog)
    }

    @Test
    fun onDeleteAllConfirmClearsNotifications() = runTest {
        val notifications =
            MutableStateFlow(
                listOf(
                    NotificationRecord(id = 3L, type = "z", timestamp = 100L, isRead = false),
                ),
            )
        val unread = MutableStateFlow(1)
        val vm = vmFor(notifications, unread)
        advanceUntilIdle()
        vm.onDeleteAllConfirm()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.notifications.isEmpty())
        assertEquals(0, vm.uiState.value.unreadCount)
        assertFalse(vm.uiState.value.showDeleteAllDialog)
    }
}
