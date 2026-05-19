package com.example.presentation.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.presentation.notification.components.NotificationDetailBodyCard
import com.example.presentation.notification.components.NotificationDetailFooter
import com.example.presentation.notification.components.NotificationDetailHeaderCard
import com.example.presentation.notification.components.notificationBodyForKind
import com.example.presentation.notification.components.notificationDetailFooterAction
import com.example.presentation.notification.components.notificationDetailIconAndTint
import com.example.presentation.notification.components.notificationDetailUsesErrorContainer
import com.example.presentation.notification.components.notificationTitleForKind
import com.example.presentation.notification.model.NotificationTypeKind
import com.example.utils.extensions.toFormattedDate

@Composable
fun NotificationDetailScreen(
    type: String,
    timestamp: Long,
    extraArg: String? = null,
    onNavigateToEventLog: (() -> Unit)? = null,
    onNavigateToHome: (() -> Unit)? = null,
) {
    val kind = NotificationTypeKind.from(type)
    NotificationDetailContent(
        kind = kind,
        timestamp = timestamp,
        extraArg = extraArg,
        onNavigateToEventLog = onNavigateToEventLog,
        onNavigateToHome = onNavigateToHome,
    )
}

@Composable
internal fun NotificationDetailContent(
    kind: NotificationTypeKind,
    timestamp: Long,
    extraArg: String?,
    onNavigateToEventLog: (() -> Unit)?,
    onNavigateToHome: (() -> Unit)?,
) {
    val title = notificationTitleForKind(kind)
    val detail = notificationBodyForKind(kind, extraArg)
    val (icon, iconTint) = notificationDetailIconAndTint(kind)
    val usesErrorContainer = notificationDetailUsesErrorContainer(kind)
    val footerAction = notificationDetailFooterAction(kind)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NotificationDetailHeaderCard(
            title = title,
            timestampLabel = timestamp.toFormattedDate(),
            icon = icon,
            iconTint = iconTint,
            usesErrorContainer = usesErrorContainer,
        )
        NotificationDetailBodyCard(detail = detail)
        NotificationDetailFooter(
            action = footerAction,
            onNavigateToEventLog = onNavigateToEventLog,
            onNavigateToHome = onNavigateToHome,
        )
    }
}
