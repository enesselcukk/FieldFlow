package com.example.presentation.home

data class HomeUiState(
    val isOnline: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasFineLocationPermission: Boolean = false,
    val hasBackgroundLocationPermission: Boolean = false,
    val batteryLevel: Int = -1,
    val isTracking: Boolean = false,
)
