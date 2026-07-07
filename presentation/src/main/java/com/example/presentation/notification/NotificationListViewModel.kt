package com.example.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.notification.DeleteAllNotificationsUseCase
import com.example.domain.usecase.notification.DeleteNotificationUseCase
import com.example.domain.usecase.notification.MarkAllNotificationsReadUseCase
import com.example.domain.usecase.notification.ObserveAllNotificationsUseCase
import com.example.domain.usecase.notification.ObserveUnreadNotificationCountUseCase
import com.example.utils.STEP_TIMEOUT_MILES
import com.example.presentation.notification.model.NotificationListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    observeAllNotifications: ObserveAllNotificationsUseCase,
    observeUnreadCount: ObserveUnreadNotificationCountUseCase,
    private val markAllRead: MarkAllNotificationsReadUseCase,
    private val deleteNotification: DeleteNotificationUseCase,
    private val deleteAllNotifications: DeleteAllNotificationsUseCase
) : ViewModel() {

    private val showDeleteAllDialog = MutableStateFlow(false)

    val uiState: StateFlow<NotificationListUiState> = combine(
        observeAllNotifications(),
        observeUnreadCount(),
        showDeleteAllDialog,
    ) { notifications, unread, showDialog ->
        NotificationListUiState(
            notifications = notifications,
            unreadCount = unread,
            showDeleteAllDialog = showDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STEP_TIMEOUT_MILES),
        initialValue = NotificationListUiState(),
    )

    fun markAllAsRead() {
        viewModelScope.launch { markAllRead() }
    }

    fun onDeleteNotification(id: Long) {
        viewModelScope.launch { deleteNotification(id) }
    }

    fun onDeleteAllClick() {
        showDeleteAllDialog.value = true
    }

    fun onDeleteAllConfirm() {
        showDeleteAllDialog.value = false
        viewModelScope.launch { deleteAllNotifications() }
    }

    fun onDeleteAllDismiss() {
        showDeleteAllDialog.value = false
    }
}
