package com.example.presentation.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.constants.NOTIF_TYPE_BATTERY
import com.example.domain.constants.NOTIF_TYPE_GEOFENCE
import com.example.domain.constants.NOTIF_TYPE_INTERNET
import com.example.domain.constants.NOTIF_TYPE_LOCATION
import com.example.presentation.R
import com.example.utils.extensions.toFormattedDate

@Composable
fun NotificationDetailScreen(
    type: String,
    timestamp: Long,
    extraArg: String? = null,
    onNavigateToEventLog: (() -> Unit)? = null,
    onNavigateToHome: (() -> Unit)? = null,
) {
    val title = when (type) {
        NOTIF_TYPE_GEOFENCE -> stringResource(R.string.notif_geofence_title)
        NOTIF_TYPE_INTERNET -> stringResource(R.string.notif_internet_lost_title)
        NOTIF_TYPE_LOCATION -> stringResource(R.string.notif_location_disabled_title)
        NOTIF_TYPE_BATTERY -> stringResource(R.string.notif_battery_low_title)
        else -> stringResource(R.string.notif_internet_lost_title)
    }

    val detail = when (type) {
        NOTIF_TYPE_GEOFENCE -> stringResource(R.string.notif_geofence_detail, extraArg ?: "")
        NOTIF_TYPE_INTERNET -> stringResource(R.string.notif_internet_lost_detail)
        NOTIF_TYPE_LOCATION -> stringResource(R.string.notif_location_disabled_detail)
        NOTIF_TYPE_BATTERY -> stringResource(R.string.notif_battery_low_detail, extraArg?.toIntOrNull() ?: 0)
        else -> ""
    }

    val icon: ImageVector = when (type) {
        NOTIF_TYPE_GEOFENCE -> Icons.Default.Warning
        NOTIF_TYPE_INTERNET -> Icons.Default.SignalWifiOff
        NOTIF_TYPE_LOCATION -> Icons.Default.LocationOff
        NOTIF_TYPE_BATTERY -> Icons.Default.BatteryAlert
        else -> Icons.Default.Info
    }

    val iconTint = when (type) {
        NOTIF_TYPE_GEOFENCE, NOTIF_TYPE_BATTERY -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (type == NOTIF_TYPE_GEOFENCE || type == NOTIF_TYPE_BATTERY)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(40.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timestamp.toFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.notif_detail_section_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        when (type) {
            NOTIF_TYPE_GEOFENCE -> {
                if (onNavigateToEventLog != null) {
                    Button(
                        onClick = onNavigateToEventLog,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.notif_detail_action_event_log))
                    }
                }
            }
            NOTIF_TYPE_INTERNET, NOTIF_TYPE_LOCATION, NOTIF_TYPE_BATTERY -> {
                if (onNavigateToHome != null) {
                    OutlinedButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.notif_detail_action_home))
                    }
                }
            }
        }
    }
}
