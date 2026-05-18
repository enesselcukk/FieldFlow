package com.example.domain.model
data class GeofenceTransition(
    val zoneId: Long,
    val zoneName: String,
    val type: GeofenceEvent.EventType,
)