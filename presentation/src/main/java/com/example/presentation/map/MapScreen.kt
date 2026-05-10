package com.example.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import com.example.presentation.R
import com.example.presentation.constants.MAPS_SHEET_PEEK_HEIGHT
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDialog by viewModel.showAddZoneDialog.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasBackgroundPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }

    val backgroundPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasBackgroundPermission = granted }

    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (hasLocationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundPermission) {
            backgroundPermLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    if (hasLocationPermission) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = MAPS_SHEET_PEEK_HEIGHT.dp,
            sheetSwipeEnabled = true,
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            containerColor = MaterialTheme.colorScheme.surface,
            content = {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    trackPoints = uiState.trackPoints,
                    currentLocation = if (uiState.isPlaybackRunning)
                        uiState.trackPoints.getOrNull(uiState.playbackIndex)
                    else uiState.currentLocation,
                    geofenceZones = uiState.geofenceZones,
                    isPlaybackRunning = uiState.isPlaybackRunning
                )
            },
            sheetContent = {
                ControlsSheetContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    isTracking = uiState.isTracking,
                    isPlaybackRunning = uiState.isPlaybackRunning,
                    hasTrackPoints = uiState.totalTrackCount >= 2,
                    geofenceZones = uiState.geofenceZones,
                    recentEvents = uiState.recentGeofenceEvents,
                    onToggleTracking = viewModel::toggleTracking,
                    onStartPlayback = { viewModel.startPlayback() },
                    onStopPlayback = { viewModel.stopPlayback() },
                    onAddZoneClick = { viewModel.onAddZoneClick() },
                    onDeleteZone = viewModel::deleteZone
                )
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            PermissionRequired(
                onGrantClick = {
                    locationPermLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
        }
    }

    if (showDialog) {
        AddZoneDialog(
            currentLocation = uiState.currentLocation,
            onDismiss = { viewModel.onDismissAddZone() },
            onConfirm = { name, lat, lng, radius ->
                viewModel.saveZone(name, lat, lng, radius)
            }
        )
    }
}

@Composable
private fun OsmMapView(
    modifier: Modifier = Modifier,
    trackPoints: List<GeoPoint>,
    currentLocation: GeoPoint?,
    geofenceZones: List<GeofenceZone>,
    isPlaybackRunning: Boolean = false
) {
    val trackLineColor = MaterialTheme.colorScheme.primary.toArgb()
    val geofenceFillColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f).toArgb()
    val geofenceOutlineColor = MaterialTheme.colorScheme.tertiary.toArgb()

    val context = LocalContext.current
    val mapView = remember { mutableStateOf<MapView?>(null) }
    val centeredOnce = remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { mapView.value?.onDetach() } }

    LaunchedEffect(Unit) {
        if (currentLocation == null) {
            try {
                val lm = context.getSystemService(LocationManager::class.java)
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )
                val lastKnown = providers
                    .mapNotNull { provider ->
                        @Suppress("MissingPermission")
                        runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
                    }
                    .maxByOrNull { it.time }

                if (lastKnown != null && !centeredOnce.value) {
                    val geoPoint = GeoPoint(lastKnown.latitude, lastKnown.longitude)
                    mapView.value?.controller?.setCenter(geoPoint)
                    centeredOnce.value = true
                }
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(currentLocation) {
        if (currentLocation != null && !centeredOnce.value) {
            mapView.value?.controller?.setCenter(currentLocation)
            centeredOnce.value = true
        }
    }

    LaunchedEffect(currentLocation) {
        if (isPlaybackRunning && currentLocation != null) {
            mapView.value?.controller?.animateTo(currentLocation)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                mapView.value = this
            }
        },
        update = { map ->
            map.overlays.clear()

            if (trackPoints.size >= 2) {
                map.overlays.add(Polyline().apply {
                    setPoints(trackPoints)
                    outlinePaint.color = trackLineColor
                    outlinePaint.strokeWidth = 10f
                    outlinePaint.isAntiAlias = true
                })
            }

            if (trackPoints.isNotEmpty()) {
                map.overlays.add(Marker(map).apply {
                    position = trackPoints.first()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Başlangıç"
                })
            }

            currentLocation?.let { loc ->
                map.overlays.add(Marker(map).apply {
                    position = loc
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Şu anki konum"
                })
            }

            for (zone in geofenceZones) {
                val center = GeoPoint(zone.centerLat, zone.centerLng)
                val circlePoints = generateCirclePoints(center, zone.radiusMeters)
                map.overlays.add(Polygon().apply {
                    points = circlePoints
                    fillPaint.color = geofenceFillColor
                    outlinePaint.color = geofenceOutlineColor
                    outlinePaint.strokeWidth = 4f
                    title = zone.name
                })
                map.overlays.add(Marker(map).apply {
                    position = center
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = zone.name
                    snippet = "${zone.radiusMeters.toInt()} m"
                })
            }

            map.invalidate()
        }
    )
}

private fun generateCirclePoints(center: GeoPoint, radiusMeters: Double, steps: Int = 64): List<GeoPoint> {
    val earthRadius = 6_371_000.0
    val lat = Math.toRadians(center.latitude)
    val lng = Math.toRadians(center.longitude)
    val d = radiusMeters / earthRadius

    return (0..steps).map { i ->
        val bearing = Math.toRadians(i * 360.0 / steps)
        val latR = asin(sin(lat) * cos(d) + cos(lat) * sin(d) * cos(bearing))
        val lngR = lng + atan2(sin(bearing) * sin(d) * cos(lat), cos(d) - sin(lat) * sin(latR))
        GeoPoint(Math.toDegrees(latR), Math.toDegrees(lngR))
    }
}

@Composable
private fun ControlsSheetContent(
    modifier: Modifier = Modifier,
    isTracking: Boolean,
    isPlaybackRunning: Boolean,
    hasTrackPoints: Boolean,
    geofenceZones: List<GeofenceZone>,
    recentEvents: List<GeofenceEvent>,
    onToggleTracking: () -> Unit,
    onStartPlayback: () -> Unit,
    onStopPlayback: () -> Unit,
    onAddZoneClick: () -> Unit,
    onDeleteZone: (Long) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
            item {
                Button(
                    onClick = onToggleTracking,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracking) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isTracking) stringResource(R.string.map_stop_tracking)
                        else stringResource(R.string.map_start_tracking),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                if (isPlaybackRunning) {
                    OutlinedButton(onClick = onStopPlayback, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.map_stop_playback))
                    }
                } else if (hasTrackPoints) {
                    OutlinedButton(onClick = onStartPlayback, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.map_start_playback))
                    }
                }
            }

            item {
                HorizontalDivider()
                OutlinedButton(onClick = onAddZoneClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.geofence_add_zone))
                }
            }

            if (geofenceZones.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.geofence_zones_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(geofenceZones) { zone ->
                    ZoneRow(zone = zone, onDelete = { onDeleteZone(zone.id) })
                }
            }

            if (recentEvents.isNotEmpty()) {
                item {
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.geofence_events_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(recentEvents.take(5)) { event ->
                    EventRow(event = event)
                }
            }
    }
}

@Composable
private fun ZoneRow(zone: GeofenceZone, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = zone.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = "${zone.radiusMeters.toInt()} m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun EventRow(event: GeofenceEvent) {
    val formatter = remember { SimpleDateFormat("HH:mm dd.MM", Locale.getDefault()) }
    val isExit = event.eventType == GeofenceEvent.EventType.EXIT
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isExit) "↗" else "↙",
            color = if (isExit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.zoneName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isExit) stringResource(R.string.geofence_event_exit)
                else stringResource(R.string.geofence_event_enter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatter.format(Date(event.timestamp)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AddZoneDialog(
    currentLocation: GeoPoint?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, lat: Double, lng: Double, radiusMeters: Double) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable {
        mutableStateOf(currentLocation?.latitude?.toString() ?: "")
    }
    var lng by rememberSaveable {
        mutableStateOf(currentLocation?.longitude?.toString() ?: "")
    }
    var radius by rememberSaveable { mutableStateOf("200") }

    val isValid = name.isNotBlank() &&
        lat.toDoubleOrNull() != null &&
        lng.toDoubleOrNull() != null &&
        (radius.toDoubleOrNull() ?: 0.0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.geofence_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.geofence_zone_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text(stringResource(R.string.geofence_latitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lng,
                    onValueChange = { lng = it },
                    label = { Text(stringResource(R.string.geofence_longitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text(stringResource(R.string.geofence_radius_meters)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name.trim(),
                        lat.toDouble(),
                        lng.toDouble(),
                        radius.toDouble()
                    )
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.geofence_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.geofence_cancel))
            }
        }
    )
}

@Composable
private fun PermissionRequired(onGrantClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.map_location_permission_required),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onGrantClick) {
                Text(stringResource(R.string.map_grant_location_permission))
            }
        }
    }
}
