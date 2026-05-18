package com.example.presentation.map.osm

import android.location.LocationManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.GeofenceZone
import com.example.presentation.R
import com.example.presentation.constants.MAP_TRACK_LINE_WIDTH_DP
import com.example.utils.permissions.hasForegroundLocationPermission
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    trackPoints: List<GeoPoint>,
    currentLocation: GeoPoint?,
    geofenceZones: List<GeofenceZone>,
    isPlaybackRunning: Boolean = false,
    allowAutomaticCameraMoves: Boolean = true,
) {
    val trackLineColor = MaterialTheme.colorScheme.primary.toArgb()
    val geofenceFillColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f).toArgb()
    val geofenceOutlineColor = MaterialTheme.colorScheme.tertiary.toArgb()

    val titleTrackStart = stringResource(R.string.map_track_start)
    val titleLocationPin = stringResource(
        if (isPlaybackRunning) R.string.map_playback_position else R.string.map_current_location,
    )

    val context = LocalContext.current
    val mapView = remember { mutableStateOf<MapView?>(null) }
    val centeredOnce = remember { mutableStateOf(false) }

    val lat = currentLocation?.latitude
    val lng = currentLocation?.longitude

    DisposableEffect(Unit) { onDispose { mapView.value?.onDetach() } }

    LaunchedEffect(allowAutomaticCameraMoves) {
        if (!allowAutomaticCameraMoves) {
            centeredOnce.value = false
        }
    }

    LaunchedEffect(mapView.value, allowAutomaticCameraMoves, lat, lng) {
        val mv = mapView.value ?: return@LaunchedEffect
        if (!allowAutomaticCameraMoves) return@LaunchedEffect
        if (!context.hasForegroundLocationPermission()) return@LaunchedEffect
        if (lat != null || lng != null) return@LaunchedEffect

        try {
            val lm = context.getSystemService(LocationManager::class.java)
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER,
            )
            val lastKnown = providers
                .mapNotNull { provider ->
                    @Suppress("MissingPermission")
                    runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
                }.maxByOrNull { it.time }

            if (lastKnown != null && !centeredOnce.value) {
                val geoPoint = GeoPoint(lastKnown.latitude, lastKnown.longitude)
                mv.controller.setCenter(geoPoint)
                centeredOnce.value = true
            }
        } catch (_: Exception) { }
    }

    LaunchedEffect(mapView.value, lat, lng, isPlaybackRunning, allowAutomaticCameraMoves) {
        val mv = mapView.value ?: return@LaunchedEffect

        if (isPlaybackRunning && lat != null && lng != null) {
            mv.controller.animateTo(GeoPoint(lat, lng))
            return@LaunchedEffect
        }

        if (!allowAutomaticCameraMoves) return@LaunchedEffect

        if (lat != null && lng != null && !centeredOnce.value) {
            mv.controller.setCenter(GeoPoint(lat, lng))
            centeredOnce.value = true
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                val poly = Polyline().apply {
                    outlinePaint.isAntiAlias = true
                }
                val markerStart = Marker(this).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                val locationMarker = Marker(this).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(poly)
                overlays.add(markerStart)
                overlays.add(locationMarker)
                tag = OsmMapTrackLayers(poly, markerStart, locationMarker)
                mapView.value = this
            }
        },
        update = { map ->
            val layers = map.tag as OsmMapTrackLayers
            val density = map.resources.displayMetrics.density
            val trackStrokePx = MAP_TRACK_LINE_WIDTH_DP * density
            val overlays = map.overlays

            layers.polyline.apply {
                outlinePaint.color = trackLineColor
                outlinePaint.strokeWidth = trackStrokePx
                outlinePaint.isAntiAlias = true
                isEnabled = trackPoints.size >= 2
                setPoints(trackPoints.toCollection(ArrayList()))
            }

            layers.markerStart.apply {
                isEnabled = trackPoints.isNotEmpty()
                title = titleTrackStart
                if (trackPoints.isNotEmpty()) position = trackPoints.first()
            }

            layers.locationMarker.apply {
                if (currentLocation != null) {
                    isEnabled = true
                    position = currentLocation
                    title = titleLocationPin
                } else {
                    isEnabled = false
                }
            }

            while (overlays.size > 3) {
                overlays.removeAt(3)
            }

            val zoneOutlinePx = 4f * density
            for (zone in geofenceZones) {
                val center = GeoPoint(zone.centerLat, zone.centerLng)
                val circlePoints = generateCirclePoints(center, zone.radiusMeters)
                overlays.add(Polygon().apply {
                    points = circlePoints
                    fillPaint.color = geofenceFillColor
                    outlinePaint.color = geofenceOutlineColor
                    outlinePaint.strokeWidth = zoneOutlinePx
                    title = zone.name
                })
                overlays.add(Marker(map).apply {
                    position = center
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = zone.name
                    snippet = "${zone.radiusMeters.toInt()} m"
                })
            }

            map.invalidate()
        },
    )
}
