package com.example.presentation.permissions

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.presentation.permissions.model.RuntimePermissionRequestHandles
import com.example.utils.ACCESS_BACKGROUND_LOCATION
import com.example.utils.POST_NOTIFICATIONS
import com.example.utils.permissions.AppRuntimePermissions
import com.example.utils.permissions.hasBackgroundLocationRuntimePermission
import com.example.utils.permissions.isForegroundLocationGranted

@Composable
fun rememberRuntimePermissionRequestHandles(
    onPermissionsSnapshotInvalidated: () -> Unit,
    onPostNotificationsResult: () -> Unit = {},
): RuntimePermissionRequestHandles {
    val context = LocalContext.current
    val onInvalidate = rememberUpdatedState(onPermissionsSnapshotInvalidated)
    val onPostNotificationsResultState = rememberUpdatedState(onPostNotificationsResult)

    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        onInvalidate.value()
    }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        onInvalidate.value()
        val foregroundGranted = results.isForegroundLocationGranted()
        val backgroundOk = context.hasBackgroundLocationRuntimePermission()
        if (foregroundGranted && !backgroundOk) {
            backgroundLauncher.launch(ACCESS_BACKGROUND_LOCATION)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        onInvalidate.value()
        onPostNotificationsResultState.value()
    }

    return RuntimePermissionRequestHandles(
        requestPostNotifications = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(POST_NOTIFICATIONS)
            }
        },
        requestForegroundLocation = {
            foregroundLauncher.launch(AppRuntimePermissions.foregroundLocation)
        },
        requestBackgroundLocation = {
            backgroundLauncher.launch(ACCESS_BACKGROUND_LOCATION)
        },
    )
}
