package com.example.domain.model

data class GeofenceTickResult(
    val states: Map<Long, ZoneGeofenceRuntimeState>,
    val transitions: List<GeofenceTransition>,
)