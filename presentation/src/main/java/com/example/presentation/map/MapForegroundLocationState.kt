package com.example.presentation.map

data class MapForegroundLocationState(
    val hasForegroundLocation: Boolean,
    val requestForegroundLocation: () -> Unit,
)
