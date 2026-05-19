package com.example.presentation.permissions.model

data class RuntimePermissionRequestHandles(
    val requestPostNotifications: () -> Unit,
    val requestForegroundLocation: () -> Unit,
    val requestBackgroundLocation: () -> Unit,
)