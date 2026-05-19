package com.example.presentation.map.model

data class MapForegroundLocationState(
    val hasForegroundLocation: Boolean,
    val requestForegroundLocation: () -> Unit,
)