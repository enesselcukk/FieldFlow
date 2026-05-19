package com.example.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.presentation.map.model.MapForegroundLocationState
import com.example.presentation.permissions.rememberRuntimePermissionRequestHandles
import com.example.utils.permissions.hasForegroundLocationPermission

@Composable
fun rememberMapForegroundLocationState(): MapForegroundLocationState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasLocationPermission by remember {
        mutableStateOf(context.hasForegroundLocationPermission())
    }

    val permissionHandles = rememberRuntimePermissionRequestHandles(
        onPermissionsSnapshotInvalidated = {
            hasLocationPermission = context.hasForegroundLocationPermission()
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = context.hasForegroundLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionHandles.requestForegroundLocation()
        }
    }

    return MapForegroundLocationState(
        hasForegroundLocation = hasLocationPermission,
        requestForegroundLocation = permissionHandles.requestForegroundLocation,
    )
}
