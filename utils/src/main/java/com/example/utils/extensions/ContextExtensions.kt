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
    val detailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val permissionsIntent = Intent(ACTION_MANAGE_APP_PERMISSIONS).apply {
            putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
            putExtra(EXTRA_PERMISSION_GROUP_NAME, Manifest.permission_group.LOCATION)
        }
        launchSettingsSafely(permissionsIntent, detailsIntent)
    } else {
        launchSettingsSafely(detailsIntent)
    }
}

fun Context.launchSettingsSafely(intent: Intent, fallback: Intent? = null) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        try {
            fallback?.let { startActivity(it) }
                ?: startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
}
