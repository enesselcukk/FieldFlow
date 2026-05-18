package com.example.fieldflow.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.fieldflow.R
import com.example.fieldflow.activation.AppActivationStore
import com.example.fieldflow.constants.EXTRA_NOTIF_EXTRA_ARG
import com.example.fieldflow.constants.EXTRA_NOTIF_TIMESTAMP
import com.example.fieldflow.constants.EXTRA_NOTIF_TYPE
import com.example.fieldflow.constants.NAV_HOME
import com.example.fieldflow.constants.NAV_NOTIFICATION_DETAIL
import com.example.presentation.notification.NotificationListViewModel
import com.example.utils.security.RootDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn

@Composable
internal fun MainNavigationHost(
    activity: ComponentActivity,
    activationStore: AppActivationStore,
    pendingNavDestination: MutableStateFlow<String?>,
    notificationViewModel: NotificationListViewModel
) {
    val scope = rememberCoroutineScope()
    val backStack = rememberNavBackStack(SplashRoute)
    var isBiometricVerified by rememberSaveable { mutableStateOf(false) }

    val router = remember(backStack) {
        MainNavRouter(
            activity = activity,
            backStack = backStack,
            scope = scope,
            activationStore = activationStore,
            setBiometricVerified = { isBiometricVerified = it }
        )
    }

    val isActivated by remember {
        activationStore.isActivated.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    }.collectAsStateWithLifecycle()

    val notifUiState by notificationViewModel.uiState.collectAsStateWithLifecycle()

    val deviceCompromised = remember { RootDetector.isDeviceCompromised() }
    var rootSecurityAcknowledged by rememberSaveable { mutableStateOf(false) }
    val showRootSecurityDialog = deviceCompromised && !rootSecurityAcknowledged

    LaunchedEffect(Unit) {
        combine(
            activationStore.isActivated,
            snapshotFlow { isBiometricVerified }
        ) { activated, biometricOk -> activated to biometricOk }
            .collect { (activated, biometricOk) ->
                val top = backStack.lastOrNull()
                when {
                    activated && biometricOk -> {
                        val leaveOnboardingForHome = when (top) {
                            SplashRoute, ScanRoute, BiometricRoute -> true
                            is ActivationRoute -> true
                            else -> false
                        }
                        if (leaveOnboardingForHome) {
                            backStack.clear()
                            backStack.add(HomeRoute)
                        }
                    }
                    activated && !biometricOk && top != BiometricRoute -> {
                        backStack.clear()
                        backStack.add(BiometricRoute)
                    }
                    !activated && top == SplashRoute -> {
                        backStack.clear()
                        backStack.add(ScanRoute)
                    }
                }
            }
    }

    LaunchedEffect(backStack) {
        combine(
            pendingNavDestination,
            snapshotFlow { isActivated },
            snapshotFlow { isBiometricVerified }
        ) { dest, activated, biometricOk -> Triple(dest, activated, biometricOk) }
            .filter { it.first != null }
            .collect { (destination, activated, biometricOk) ->
                val dest = destination!!
                if (!activated || !biometricOk) return@collect
                val intent: Intent = activity.intent
                when (dest) {
                    NAV_NOTIFICATION_DETAIL -> {
                        val type = intent.getStringExtra(EXTRA_NOTIF_TYPE) ?: return@collect
                        val timestamp = intent.getLongExtra(EXTRA_NOTIF_TIMESTAMP, System.currentTimeMillis())
                        val extraArg = intent.getStringExtra(EXTRA_NOTIF_EXTRA_ARG)
                        if (backStack.lastOrNull() !is HomeRoute) {
                            backStack.clear()
                            backStack.add(HomeRoute)
                        }
                        backStack.add(NotificationDetailRoute(type, timestamp, extraArg))
                    }
                    NAV_HOME -> {
                        backStack.clear()
                        backStack.add(HomeRoute)
                    }
                }
                pendingNavDestination.value = null
            }
    }

    val currentRoute = backStack.lastOrNull() as? FieldFlowRoute

    Scaffold(
        topBar = {
            MainTopBar(
                currentRoute = currentRoute,
                backStackSize = backStack.size,
                isActivated = isActivated,
                isBiometricVerified = isBiometricVerified,
                notifUiState = notifUiState,
                onBack = { backStack.removeLastOrNull() },
                onNotificationsClick = router::navigateToNotificationList,
                onSettingsClick = router::navigateToSettings
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MainNavDisplay(
                activity = activity,
                backStack = backStack,
                router = router,
                activationStore = activationStore,
                isActivated = isActivated,
                notifUiState = notifUiState,
                notificationViewModel = notificationViewModel
            )
        }
    }

    if (showRootSecurityDialog) {
        val acknowledgeRootNotice = { rootSecurityAcknowledged = true }
        val title = stringResource(R.string.security_root_dialog_title)
        val message = stringResource(R.string.security_root_warning)
        AlertDialog(
            onDismissRequest = acknowledgeRootNotice,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = acknowledgeRootNotice) {
                    Text(stringResource(R.string.security_root_acknowledge))
                }
            }
        )
    }
}
