package com.example.presentation.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.presentation.R

@Composable
internal fun notificationTitleForKind(kind: NotificationTypeKind): String =
    when (kind) {
        NotificationTypeKind.Geofence -> stringResource(R.string.notif_geofence_title)
        NotificationTypeKind.Internet -> stringResource(R.string.notif_internet_lost_title)
        NotificationTypeKind.Location -> stringResource(R.string.notif_location_disabled_title)
        NotificationTypeKind.Battery -> stringResource(R.string.notif_battery_low_title)
        NotificationTypeKind.Unknown -> stringResource(R.string.notif_internet_lost_title)
    }

@Composable
internal fun notificationBodyForKind(kind: NotificationTypeKind, extraArg: String?): String =
    when (kind) {
        NotificationTypeKind.Geofence ->
            stringResource(R.string.notif_geofence_detail, extraArg ?: "")

        NotificationTypeKind.Internet -> stringResource(R.string.notif_internet_lost_detail)
        NotificationTypeKind.Location -> stringResource(R.string.notif_location_disabled_detail)
        NotificationTypeKind.Battery ->
            stringResource(R.string.notif_battery_low_detail, extraArg?.toIntOrNull() ?: 0)

        NotificationTypeKind.Unknown -> ""
    }

@Composable
internal fun notificationListIconAndTint(kind: NotificationTypeKind): Pair<ImageVector, Color> {
    val error = MaterialTheme.colorScheme.error
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    return when (kind) {
        NotificationTypeKind.Geofence -> Icons.Default.Warning to error
        NotificationTypeKind.Internet -> Icons.Default.SignalWifiOff to primary
        NotificationTypeKind.Location -> Icons.Default.LocationOff to tertiary
        NotificationTypeKind.Battery -> Icons.Default.BatteryAlert to error
        NotificationTypeKind.Unknown -> Icons.Default.Info to primary
    }
}

@Composable
internal fun notificationDetailIconAndTint(kind: NotificationTypeKind): Pair<ImageVector, Color> {
    val error = MaterialTheme.colorScheme.error
    val primary = MaterialTheme.colorScheme.primary
    return when (kind) {
        NotificationTypeKind.Geofence -> Icons.Default.Warning to error
        NotificationTypeKind.Internet -> Icons.Default.SignalWifiOff to primary
        NotificationTypeKind.Location -> Icons.Default.LocationOff to primary
        NotificationTypeKind.Battery -> Icons.Default.BatteryAlert to error
        NotificationTypeKind.Unknown -> Icons.Default.Info to primary
    }
}

internal fun notificationDetailUsesErrorContainer(kind: NotificationTypeKind): Boolean =
    kind == NotificationTypeKind.Geofence || kind == NotificationTypeKind.Battery

internal fun notificationDetailFooterAction(kind: NotificationTypeKind): NotificationDetailFooterAction =
    when (kind) {
        NotificationTypeKind.Geofence -> NotificationDetailFooterAction.EventLog
        NotificationTypeKind.Internet,
        NotificationTypeKind.Location,
        NotificationTypeKind.Battery,
        -> NotificationDetailFooterAction.Home

        NotificationTypeKind.Unknown -> NotificationDetailFooterAction.None
    }
