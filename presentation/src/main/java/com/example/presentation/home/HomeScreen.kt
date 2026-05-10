package com.example.presentation.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.utils.extensions.launchSettingsSafely
import com.example.utils.extensions.openAppNotificationSettings

@Composable
fun HomeScreen(
    message: String,
    onNavigateToMap: () -> Unit = {},
    onNavigateToEventLog: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshRuntimePermissions() }

    val fineLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.refreshRuntimePermissions()
        val fineJustGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val backgroundAlreadyGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        if (fineJustGranted && !backgroundAlreadyGranted) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshRuntimePermissions()
        val fineAlreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineAlreadyGranted) {
            fineLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshRuntimePermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        when {
            !uiState.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            !uiState.hasFineLocationPermission ->
                fineLocationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        StatusCard(
            title = stringResource(R.string.status_internet_title),
            description = if (uiState.isOnline) stringResource(R.string.status_internet_online)
            else stringResource(R.string.status_internet_offline),
            isOk = uiState.isOnline,
            actionLabel = stringResource(R.string.status_internet_action),
            onFixClick = { context.launchSettingsSafely(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
        )

        StatusCard(
            title = stringResource(R.string.status_location_title),
            description = if (uiState.isLocationEnabled) stringResource(R.string.status_location_on)
            else stringResource(R.string.status_location_off),
            isOk = uiState.isLocationEnabled,
            actionLabel = stringResource(R.string.status_location_action),
            onFixClick = { context.launchSettingsSafely(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            StatusCard(
                title = stringResource(R.string.status_background_location_title),
                description = if (uiState.hasBackgroundLocationPermission)
                    stringResource(R.string.status_background_location_granted)
                else
                    stringResource(R.string.status_background_location_denied),
                isOk = uiState.hasBackgroundLocationPermission,
                actionLabel = stringResource(R.string.status_background_location_action),
                onFixClick = { backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
            )
        }

        StatusCard(
            title = stringResource(R.string.status_notification_title),
            description = if (uiState.hasNotificationPermission)
                stringResource(R.string.status_notification_granted)
            else
                stringResource(R.string.status_notification_denied),
            isOk = uiState.hasNotificationPermission,
            actionLabel = stringResource(R.string.status_notification_action),
            onFixClick = { context.openAppNotificationSettings() }
        )

        BatteryStatusCard(batteryLevel = uiState.batteryLevel)

        TrackingControlCard(
            isTracking = uiState.isTracking,
            onToggleTracking = viewModel::toggleTracking,
            onNavigateToMap = onNavigateToMap,
            onNavigateToEventLog = onNavigateToEventLog
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatusCard(
    title: String,
    description: String,
    isOk: Boolean,
    actionLabel: String,
    onFixClick: () -> Unit
) {
    val containerColor = if (isOk)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    val contentColor = if (isOk)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
            if (!isOk) {
                TextButton(onClick = onFixClick) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingControlCard(
    isTracking: Boolean,
    onToggleTracking: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToEventLog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.map_start_tracking).let {
                        if (isTracking) stringResource(R.string.tracking_active)
                        else stringResource(R.string.tracking_inactive)
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleTracking,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracking)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isTracking)
                            stringResource(R.string.map_stop_tracking)
                        else
                            stringResource(R.string.map_start_tracking),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = onNavigateToMap,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.open_map))
                }
            }
            OutlinedButton(
                onClick = onNavigateToEventLog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.open_event_log))
            }
        }
    }
}

@Composable
private fun BatteryStatusCard(batteryLevel: Int) {
    val isLow = batteryLevel in 0..20
    val containerColor = when {
        batteryLevel < 0 -> MaterialTheme.colorScheme.surfaceVariant
        isLow -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when {
        batteryLevel < 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        isLow -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val description = when {
        batteryLevel < 0 -> stringResource(R.string.status_battery_measuring)
        isLow -> stringResource(R.string.status_battery_low, batteryLevel)
        else -> stringResource(R.string.status_battery_normal, batteryLevel)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isLow && batteryLevel >= 0) Icons.Default.Warning
                else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.status_battery_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
        }
    }
}

