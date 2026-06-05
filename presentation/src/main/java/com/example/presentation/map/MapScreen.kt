package com.example.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.constants.MAPS_SHEET_PEEK_HEIGHT
import com.example.presentation.map.components.AddZoneDialog
import com.example.presentation.map.components.MapControlsSheetContent
import com.example.presentation.map.components.MapPermissionRequired
import com.example.presentation.map.osm.OsmMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDialog by viewModel.showAddZoneDialog.collectAsStateWithLifecycle()

    val locationState = rememberMapForegroundLocationState(
        onForegroundLocationRevoked = { viewModel.onForegroundLocationAccessChanged(false) },
    )

    LaunchedEffect(locationState.hasForegroundLocation) {
        if (!locationState.hasForegroundLocation) {
            viewModel.onForegroundLocationAccessChanged(false)
        }
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    val hasForegroundLocation = locationState.hasForegroundLocation
    val mapPin = when {
        uiState.isPlaybackRunning -> uiState.trackPoints.getOrNull(uiState.playbackIndex)
        hasForegroundLocation -> uiState.currentLocation
        else -> null
    }
    val allowAutomaticCameraMoves = hasForegroundLocation || uiState.isPlaybackRunning

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = MAPS_SHEET_PEEK_HEIGHT.dp,
        sheetSwipeEnabled = true,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        containerColor = MaterialTheme.colorScheme.surface,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    trackPoints = uiState.trackPoints,
                    currentLocation = mapPin,
                    geofenceZones = uiState.geofenceZones,
                    isPlaybackRunning = uiState.isPlaybackRunning,
                    allowAutomaticCameraMoves = allowAutomaticCameraMoves,
                )
                if (!hasForegroundLocation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.52f)),
                    )
                    MapPermissionRequired(onGrantClick = locationState.requestForegroundLocation)
                }
            }
        },
        sheetContent = {
            MapControlsSheetContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                isTracking = uiState.isTracking,
                isPlaybackRunning = uiState.isPlaybackRunning,
                hasTrackPoints = uiState.totalTrackCount >= 2,
                geofenceZones = uiState.geofenceZones,
                recentEvents = uiState.recentGeofenceEvents,
                onToggleTracking = viewModel::toggleTracking,
                onStartPlayback = viewModel::startPlayback,
                onStopPlayback = viewModel::stopPlayback,
                onAddZoneClick = viewModel::onAddZoneClick,
                onDeleteZone = viewModel::deleteZone,
            )
        },
    )

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
