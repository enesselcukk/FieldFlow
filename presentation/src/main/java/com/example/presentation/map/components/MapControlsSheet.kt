package com.example.presentation.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import com.example.presentation.R

@Composable
fun MapControlsSheetContent(
    modifier: Modifier = Modifier,
    isTracking: Boolean,
    isPlaybackRunning: Boolean,
    hasTrackPoints: Boolean,
    geofenceZones: List<GeofenceZone>,
    recentEvents: List<GeofenceEvent>,
    onToggleTracking: () -> Unit,
    onStartPlayback: () -> Unit,
    onStopPlayback: () -> Unit,
    onAddZoneClick: () -> Unit,
    onDeleteZone: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.padding(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Button(
                onClick = onToggleTracking,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isTracking) stringResource(R.string.map_stop_tracking)
                    else stringResource(R.string.map_start_tracking),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        item {
            if (isPlaybackRunning) {
                OutlinedButton(onClick = onStopPlayback, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.map_stop_playback))
                }
            } else if (hasTrackPoints) {
                OutlinedButton(onClick = onStartPlayback, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.map_start_playback))
                }
            }
        }

        item {
            HorizontalDivider()
            OutlinedButton(onClick = onAddZoneClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.geofence_add_zone))
            }
        }

        if (geofenceZones.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.geofence_zones_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(geofenceZones) { zone ->
                GeofenceZoneRow(zone = zone, onDelete = { onDeleteZone(zone.id) })
            }
        }

        if (recentEvents.isNotEmpty()) {
            item {
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.geofence_events_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(recentEvents.take(5)) { event ->
                GeofenceEventRow(event = event)
            }
        }
    }
}
