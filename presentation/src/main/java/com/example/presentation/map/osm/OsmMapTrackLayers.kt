package com.example.presentation.map.osm

import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

internal data class OsmMapTrackLayers(
    val polyline: Polyline,
    val markerStart: Marker,
    val locationMarker: Marker,
)
