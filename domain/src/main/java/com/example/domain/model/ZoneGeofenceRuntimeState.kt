package com.example.domain.model

data class ZoneGeofenceRuntimeState(
    val logicalInside: Boolean,
    val exitStreak: Int = 0,
    val enterStreak: Int = 0,
)