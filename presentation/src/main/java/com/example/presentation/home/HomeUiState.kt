package com.example.presentation.home

data class HomeUiState(
    val isOnline: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val batteryLevel: Int = -1,
)
