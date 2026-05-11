package com.example.domain.model

data class RuntimePermissions(
    val hasNotificationPermission: Boolean,
    val hasFineLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
)
