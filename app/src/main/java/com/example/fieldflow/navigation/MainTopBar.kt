package com.example.fieldflow.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.example.fieldflow.R
import com.example.presentation.R as PresentationR
import com.example.presentation.constants.NOTIFICATION_BADGE_MAX
import com.example.presentation.notification.NotificationListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainTopBar(
    currentRoute: NavKey?,
    backStackSize: Int,
    isActivated: Boolean,
    isBiometricVerified: Boolean,
    notifUiState: NotificationListUiState,
    onBack: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val topBarTitle = when (currentRoute) {
        is ScanRoute -> stringResource(R.string.topbar_scan)
        is ActivationRoute -> stringResource(R.string.topbar_activation)
        is BiometricRoute -> stringResource(R.string.topbar_biometric)
        is HomeRoute -> stringResource(R.string.topbar_home)
        is MapRoute -> stringResource(PresentationR.string.topbar_map)
        is EventLogRoute -> stringResource(PresentationR.string.topbar_event_log)
        is SettingsRoute -> stringResource(R.string.topbar_settings)
        is NotificationDetailRoute -> stringResource(R.string.topbar_notification_detail)
        is NotificationListRoute -> stringResource(PresentationR.string.topbar_notification_list)
        else -> stringResource(R.string.app_name)
    }

    val showTopBar = currentRoute !is SplashRoute
    val canGoBack = backStackSize > 1 &&
        currentRoute !is HomeRoute &&
        currentRoute !is SplashRoute
    val showSettingsIcon = showTopBar && currentRoute !is SettingsRoute
    val showNotificationBell = showTopBar &&
        isActivated && isBiometricVerified &&
        currentRoute !is NotificationListRoute

    if (!showTopBar) return

    TopAppBar(
        title = {
            Text(
                text = topBarTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            }
        },
        actions = {
            if (showNotificationBell) {
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (notifUiState.unreadCount > 0) {
                                Badge {
                                    Text(
                                        text = if (notifUiState.unreadCount > NOTIFICATION_BADGE_MAX) {
                                            "${NOTIFICATION_BADGE_MAX}+"
                                        } else {
                                            notifUiState.unreadCount.toString()
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(PresentationR.string.topbar_notification_list)
                        )
                    }
                }
            }
            if (showSettingsIcon) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_icon_desc)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
