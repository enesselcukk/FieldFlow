package com.example.presentation.map.model

import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import org.osmdroid.util.GeoPoint

data class MapUiState(
    val currentLocation: GeoPoint? = null,
    val trackPoints: List<GeoPoint> = emptyList(),
    val totalTrackCount: Int = 0,
    val isPlaybackRunning: Boolean = false,
    val playbackIndex: Int = 0,
    val geofenceZones: List<GeofenceZone> = emptyList(),
    val recentGeofenceEvents: List<GeofenceEvent> = emptyList(),
    val isTracking: Boolean = false,
)