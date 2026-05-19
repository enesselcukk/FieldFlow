package com.example.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.notification.DeleteAllNotificationsUseCase
import com.example.domain.usecase.notification.DeleteNotificationUseCase
import com.example.domain.usecase.notification.MarkAllNotificationsReadUseCase
import com.example.domain.usecase.notification.ObserveAllNotificationsUseCase
import com.example.domain.usecase.notification.ObserveUnreadNotificationCountUseCase
import com.example.presentation.notification.model.NotificationListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val observeAllNotifications: ObserveAllNotificationsUseCase,
    private val observeUnreadCount: ObserveUnreadNotificationCountUseCase,
    private val markAllRead: MarkAllNotificationsReadUseCase,
    private val deleteNotification: DeleteNotificationUseCase,
    private val deleteAllNotifications: DeleteAllNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationListUiState())
    val uiState: StateFlow<NotificationListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeAllNotifications(),
                observeUnreadCount()
            ) { notifications, unread ->
                _uiState.value.copy(notifications = notifications, unreadCount = unread)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch { markAllRead() }
    }

    fun onDeleteNotification(id: Long) {
        viewModelScope.launch { deleteNotification(id) }
    }

    fun onDeleteAllClick() {
        _uiState.update { it.copy(showDeleteAllDialog = true) }
    }

    fun onDeleteAllConfirm() {
        _uiState.update { it.copy(showDeleteAllDialog = false) }
        viewModelScope.launch { deleteAllNotifications() }
    }

    fun onDeleteAllDismiss() {
        _uiState.update { it.copy(showDeleteAllDialog = false) }
    }
}
