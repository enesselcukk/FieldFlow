package com.example.utils.permissions

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.domain.model.RuntimePermissions
import com.example.utils.ACCESS_BACKGROUND_LOCATION
import com.example.utils.CAMERA
import com.example.utils.POST_NOTIFICATIONS

fun Context.snapshotRuntimePermissions(): RuntimePermissions =
    RuntimePermissions(
        hasNotificationPermission = this.canPostNotifications(),
        hasForegroundLocationPermission = this.hasForegroundLocationPermission(),
        hasBackgroundLocationPermission = this.hasBackgroundLocationRuntimePermission(),
    )

fun Context.hasNotificationRuntimePermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

fun Context.areAppNotificationsEnabled(): Boolean {
    val notificationManager = getSystemService(NotificationManager::class.java) ?: return false
    return notificationManager.areNotificationsEnabled()
}

fun Context.canPostNotifications(): Boolean =
    hasNotificationRuntimePermission() && areAppNotificationsEnabled()

fun Context.hasForegroundLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

fun Context.hasBackgroundLocationRuntimePermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, CAMERA) ==
        PackageManager.PERMISSION_GRANTED

fun Map<String, Boolean>.isForegroundLocationGranted(): Boolean =
    this[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        this[Manifest.permission.ACCESS_COARSE_LOCATION] == true
