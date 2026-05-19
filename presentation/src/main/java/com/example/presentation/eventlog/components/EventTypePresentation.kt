package com.example.presentation.eventlog.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.domain.model.EventRecord
import com.example.presentation.R

internal data class EventVisuals(
    val icon: ImageVector,
    val tint: Color,
    val containerColor: Color,
)

@Composable
internal fun eventVisuals(type: EventRecord.EventType): EventVisuals {
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surfaceVariant

    return when (type) {
        EventRecord.EventType.GEOFENCE_EXIT -> EventVisuals(
            icon = Icons.Default.Warning,
            tint = errorColor,
            containerColor = errorContainer,
        )
        EventRecord.EventType.GEOFENCE_ENTER -> EventVisuals(
            icon = Icons.Default.LocationOn,
            tint = primaryColor,
            containerColor = primaryContainer,
        )
        EventRecord.EventType.INTERNET_LOST -> EventVisuals(
            icon = Icons.Default.Warning,
            tint = errorColor,
            containerColor = errorContainer,
        )
        EventRecord.EventType.INTERNET_RESTORED -> EventVisuals(
            icon = Icons.Default.LocationOn,
            tint = tertiaryColor,
            containerColor = surface,
        )
        EventRecord.EventType.LOCATION_SERVICE_DISABLED -> EventVisuals(
            icon = Icons.Default.Warning,
            tint = errorColor,
            containerColor = errorContainer,
        )
        EventRecord.EventType.LOCATION_SERVICE_ENABLED -> EventVisuals(
            icon = Icons.Default.LocationOn,
            tint = primaryColor,
            containerColor = primaryContainer,
        )
    }
}

@Composable
internal fun EventRecord.EventType.toLabel(): String = when (this) {
    EventRecord.EventType.GEOFENCE_EXIT -> stringResource(R.string.event_type_geofence_exit)
    EventRecord.EventType.GEOFENCE_ENTER -> stringResource(R.string.event_type_geofence_enter)
    EventRecord.EventType.INTERNET_LOST -> stringResource(R.string.event_type_internet_lost)
    EventRecord.EventType.INTERNET_RESTORED -> stringResource(R.string.event_type_internet_restored)
    EventRecord.EventType.LOCATION_SERVICE_DISABLED -> stringResource(R.string.event_type_location_disabled)
    EventRecord.EventType.LOCATION_SERVICE_ENABLED -> stringResource(R.string.event_type_location_enabled)
}
