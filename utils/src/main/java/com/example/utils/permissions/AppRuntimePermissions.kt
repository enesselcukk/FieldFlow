package com.example.utils.permissions

import android.Manifest

object AppRuntimePermissions {
    val foregroundLocation: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    const val postNotifications: String = Manifest.permission.POST_NOTIFICATIONS
    const val camera: String = Manifest.permission.CAMERA
}
