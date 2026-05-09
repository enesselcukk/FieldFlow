package com.example.domain.model

data class GeofenceZone(
    val id: Long = 0,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Double
)
