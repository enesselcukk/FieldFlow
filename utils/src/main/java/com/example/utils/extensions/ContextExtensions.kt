package com.example.utils.extensions

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.utils.ACTION_APP_PERMISSION_SETTINGS
import com.example.utils.ACTION_MANAGE_APP_PERMISSIONS
import com.example.utils.EXTRA_PERMISSION_GROUP_NAME


fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

fun Context.openAppNotificationSettings() {
    launchSettingsSafely(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
    )
}

fun Context.openAppDetailsSettings() {
    launchSettingsSafely(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        },
    )
}

fun Context.openAppLocationPermissionSettings() {
    val packageUri = Uri.fromParts("package", packageName, null)
    val appPermissionsIntent = Intent(ACTION_APP_PERMISSION_SETTINGS).apply {
        data = packageUri
    }
    val detailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = packageUri
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val locationGroupIntent = Intent(ACTION_MANAGE_APP_PERMISSIONS).apply {
            putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
            putExtra(EXTRA_PERMISSION_GROUP_NAME, Manifest.permission_group.LOCATION)
        }
        launchSettingsSafely(appPermissionsIntent, locationGroupIntent, detailsIntent)
    } else {
        launchSettingsSafely(appPermissionsIntent, detailsIntent)
    }
}

fun Context.launchSettingsSafely(primary: Intent, vararg fallbacks: Intent) {
    for (intent in listOf(primary) + fallbacks.toList()) {
        try {
            startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
        }
    }
    try {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    } catch (_: ActivityNotFoundException) {
    }
}
