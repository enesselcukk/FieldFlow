package com.example.presentation.home.dashboard

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.home.components.BatteryStatusCard
import com.example.presentation.home.components.StatusCard
import com.example.presentation.home.components.TrackingControlCard
import com.example.presentation.home.model.HomeUiState
import com.example.utils.extensions.launchSettingsSafely
import com.example.utils.extensions.openAppLocationPermissionSettings
import com.example.utils.extensions.openAppNotificationSettings

@Composable
internal fun HomeDashboard(
    uiState: HomeUiState,
    onToggleTracking: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToEventLog: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.app_activated_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        StatusCard(
            title = stringResource(R.string.status_internet_title),
            description = if (uiState.isOnline) stringResource(R.string.status_internet_online)
            else stringResource(R.string.status_internet_offline),
            isOk = uiState.isOnline,
            actionLabel = stringResource(R.string.status_internet_action),
            onFixClick = { context.launchSettingsSafely(Intent(Settings.ACTION_WIRELESS_SETTINGS)) },
        )

        StatusCard(
            title = stringResource(R.string.status_location_title),
            description = if (uiState.isLocationEnabled) stringResource(R.string.status_location_on)
            else stringResource(R.string.status_location_off),
            isOk = uiState.isLocationEnabled,
            actionLabel = stringResource(R.string.status_location_action),
            onFixClick = { context.launchSettingsSafely(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            StatusCard(
                title = stringResource(R.string.status_background_location_title),
                description = if (uiState.hasBackgroundLocationPermission) {
                    stringResource(R.string.status_background_location_granted)
                } else {
                    stringResource(R.string.status_background_location_denied)
                },
                isOk = uiState.hasBackgroundLocationPermission,
                actionLabel = stringResource(R.string.status_background_location_action),
                onFixClick = { context.openAppLocationPermissionSettings() },
            )
        }

        StatusCard(
            title = stringResource(R.string.status_notification_title),
            description = if (uiState.hasNotificationPermission) {
                stringResource(R.string.status_notification_granted)
            } else {
                stringResource(R.string.status_notification_denied)
            },
            isOk = uiState.hasNotificationPermission,
            actionLabel = stringResource(R.string.status_notification_action),
            onFixClick = { context.openAppNotificationSettings() },
        )

        BatteryStatusCard(batteryLevel = uiState.batteryLevel)

        TrackingControlCard(
            isTracking = uiState.isTracking,
            onToggleTracking = onToggleTracking,
            onNavigateToMap = onNavigateToMap,
            onNavigateToEventLog = onNavigateToEventLog,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
