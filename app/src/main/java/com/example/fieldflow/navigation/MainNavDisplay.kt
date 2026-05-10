package com.example.fieldflow.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.fieldflow.R
import com.example.presentation.auth.activation.ActivationCodeScreen
import com.example.presentation.auth.biometric.BiometricAuthScreen
import com.example.presentation.auth.idscan.IdScanScreen
import com.example.presentation.eventlog.EventLogScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.map.MapScreen
import com.example.presentation.notification.NotificationDetailScreen
import com.example.presentation.notification.NotificationListScreen
import com.example.presentation.notification.NotificationListUiState
import com.example.presentation.notification.NotificationListViewModel
import com.example.presentation.settings.SettingsScreen

@Composable
internal fun MainNavDisplay(
    activity: ComponentActivity,
    backStack: NavBackStack<NavKey>,
    router: MainNavRouter,
    isActivated: Boolean,
    notifUiState: NotificationListUiState,
    notificationViewModel: NotificationListViewModel,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<SplashRoute> {
                Box(modifier = Modifier.fillMaxSize())
            }

            entry<ScanRoute> {
                IdScanScreen(onIdentityDetected = router::onIdentityDetected)
            }

            entry<ActivationRoute> {
                ActivationCodeScreen(
                    expectedCode = MainNavRouter.ACTIVATION_CODE,
                    onActivationSuccess = router::onActivationCodeSuccess
                )
            }

            entry<BiometricRoute> {
                BiometricAuthScreen(onAuthenticated = router::onBiometricAuthenticated)
            }

            entry<HomeRoute> {
                HomeScreen(
                    message = activity.getString(R.string.app_activated_message),
                    onNavigateToMap = router::navigateToMap,
                    onNavigateToEventLog = router::navigateToEventLog
                )
            }

            entry<EventLogRoute> {
                EventLogScreen()
            }

            entry<MapRoute> {
                MapScreen()
            }

            entry<SettingsRoute> {
                SettingsScreen(isActivated = isActivated)
            }

            entry<NotificationDetailRoute> { route ->
                NotificationDetailScreen(
                    type = route.type,
                    timestamp = route.timestamp,
                    extraArg = route.extraArg,
                    onNavigateToEventLog = router::popNotificationDetailThenEventLog,
                    onNavigateToHome = router::clearToHome
                )
            }

            entry<NotificationListRoute> {
                NotificationListScreen(
                    uiState = notifUiState,
                    onDeleteNotification = notificationViewModel::onDeleteNotification,
                    onDeleteAllClick = notificationViewModel::onDeleteAllClick,
                    onDeleteAllConfirm = notificationViewModel::onDeleteAllConfirm,
                    onDeleteAllDismiss = notificationViewModel::onDeleteAllDismiss,
                    onMarkAllRead = notificationViewModel::markAllAsRead,
                    onNotificationClick = router::openNotificationDetail
                )
            }
        },
        modifier = modifier
    )
}
