package com.example.domain.model

data class RuntimePermissions(
    val hasNotificationPermission: Boolean,
    val hasForegroundLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
)
