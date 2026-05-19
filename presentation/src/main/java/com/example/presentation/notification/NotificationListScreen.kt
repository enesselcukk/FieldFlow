package com.example.presentation.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.NotificationRecord
import com.example.presentation.notification.components.DeleteAllNotificationsDialog
import com.example.presentation.notification.components.NotificationEmptyState
import com.example.presentation.notification.components.NotificationListToolbar
import com.example.presentation.notification.components.SwipeableNotificationRow
import com.example.presentation.notification.model.NotificationListUiState

@Composable
fun NotificationListScreen(
    uiState: NotificationListUiState,
    onDeleteNotification: (Long) -> Unit,
    onDeleteAllClick: () -> Unit,
    onDeleteAllConfirm: () -> Unit,
    onDeleteAllDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (NotificationRecord) -> Unit,
) {
    LaunchedEffect(Unit) { onMarkAllRead() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.notifications.isEmpty()) {
            NotificationEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 88.dp,
                ),
            ) {
                item {
                    NotificationListToolbar(onDeleteAllClick = onDeleteAllClick)
                }
                items(
                    items = uiState.notifications,
                    key = { it.id },
                ) { record ->
                    SwipeableNotificationRow(
                        record = record,
                        onClick = { onNotificationClick(record) },
                        onDelete = { onDeleteNotification(record.id) },
                    )
                }
            }
        }
    }

    if (uiState.showDeleteAllDialog) {
        DeleteAllNotificationsDialog(
            onConfirm = onDeleteAllConfirm,
            onDismiss = onDeleteAllDismiss,
        )
    }
}
