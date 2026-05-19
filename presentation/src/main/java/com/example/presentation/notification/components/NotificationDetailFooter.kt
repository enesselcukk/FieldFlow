package com.example.presentation.notification.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.presentation.R
import com.example.presentation.notification.model.NotificationDetailFooterAction

@Composable
internal fun NotificationDetailFooter(
    action: NotificationDetailFooterAction,
    onNavigateToEventLog: (() -> Unit)?,
    onNavigateToHome: (() -> Unit)?,
) {
    when (action) {
        NotificationDetailFooterAction.EventLog -> {
            if (onNavigateToEventLog != null) {
                Button(
                    onClick = onNavigateToEventLog,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.notif_detail_action_event_log))
                }
            }
        }
        NotificationDetailFooterAction.Home -> {
            if (onNavigateToHome != null) {
                OutlinedButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.notif_detail_action_home))
                }
            }
        }
        NotificationDetailFooterAction.None -> Unit
    }
}
