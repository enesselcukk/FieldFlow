package com.example.presentation.home.runtime

import android.os.Build
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.presentation.constants.AutoPermissionCooldownMs
import com.example.presentation.constants.PermissionResumeDebounceMs
import com.example.presentation.home.HomeViewModel
import com.example.presentation.home.dashboard.HomeDashboard
import com.example.presentation.home.model.HomeUiState
import com.example.presentation.permissions.rememberRuntimePermissionRequestHandles
import com.example.utils.extensions.findActivity
import com.example.utils.permissions.snapshotRuntimePermissions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Composable
internal fun HomeRuntimePermissionHost(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    message: String,
    onNavigateToMap: () -> Unit,
    onNavigateToEventLog: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner =
        context.findActivity() as? LifecycleOwner ?: LocalLifecycleOwner.current

    var lastPostNotificationsPromptAt by remember { mutableLongStateOf(0L) }
    var lastForegroundPromptAt by remember { mutableLongStateOf(0L) }

    var postNotificationsPromptFinished by rememberSaveable { mutableStateOf(false) }

    val permissionHandles = rememberRuntimePermissionRequestHandles(
        onPermissionsSnapshotInvalidated = viewModel::refreshRuntimePermissions,
        onPostNotificationsResult = { postNotificationsPromptFinished = true },
    )
    val handlesState = rememberUpdatedState(permissionHandles)

    val foregroundOk = uiState.hasForegroundLocationPermission
    LaunchedEffect(foregroundOk) {
        if (!foregroundOk) {
            viewModel.onForegroundLocationAccessChanged(false)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        val resumeSignals = Channel<Unit>(Channel.CONFLATED)
        val lifecycle = lifecycleOwner.lifecycle
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    resumeSignals.trySend(Unit)
                }
            }
        lifecycle.addObserver(observer)
        try {
            while (true) {
                resumeSignals.receive()
                delay(PermissionResumeDebounceMs)
                yield()
                viewModel.refreshRuntimePermissions()
                val snapshot = context.snapshotRuntimePermissions()
                val handles = handlesState.value
                val now = SystemClock.elapsedRealtime()

                val notificationMissing =
                    !snapshot.hasNotificationPermission &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                when {
                    notificationMissing &&
                        !postNotificationsPromptFinished &&
                        now - lastPostNotificationsPromptAt >= AutoPermissionCooldownMs -> {
                        lastPostNotificationsPromptAt = now
                        handles.requestPostNotifications()
                    }

                    (!notificationMissing || postNotificationsPromptFinished) &&
                        !snapshot.hasForegroundLocationPermission &&
                        now - lastForegroundPromptAt >= AutoPermissionCooldownMs -> {
                        lastForegroundPromptAt = now
                        handles.requestForegroundLocation()
                    }
                }
            }
        } finally {
            lifecycle.removeObserver(observer)
        }
    }

    HomeDashboard(
        uiState = uiState,
        message = message,
        onToggleTracking = viewModel::toggleTracking,
        onNavigateToMap = onNavigateToMap,
        onNavigateToEventLog = onNavigateToEventLog,
    )
}
