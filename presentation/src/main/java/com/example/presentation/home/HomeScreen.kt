package com.example.presentation.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.presentation.R
import com.example.utils.extensions.openAppNotificationSettings
import com.example.utils.extensions.launchSettingsSafely
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    message: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshRuntimePermissions() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshRuntimePermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.hasNotificationPermission) {
        if (!uiState.hasNotificationPermission &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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

        StatusCard(
            title = stringResource(R.string.status_background_title),
            description = if (uiState.isBatteryOptimizationIgnored)
                stringResource(R.string.status_background_granted)
            else
                stringResource(R.string.status_background_restricted),
            isOk = uiState.isBatteryOptimizationIgnored,
            actionLabel = stringResource(R.string.status_background_action),
            onFixClick = {
                context.launchSettingsSafely(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    },
                    fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                )
            }
        )

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

