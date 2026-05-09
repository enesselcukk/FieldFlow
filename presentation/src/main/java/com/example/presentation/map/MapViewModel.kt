package com.example.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.GeofenceZone
import com.example.domain.repository.TrackingRepository
import com.example.domain.usecase.DeleteGeofenceZoneUseCase
import com.example.domain.usecase.ObserveGeofenceZonesUseCase
import com.example.domain.usecase.ObserveRecentGeofenceEventsUseCase
import com.example.domain.usecase.ObserveRecentLocationsUseCase
import com.example.domain.usecase.SaveGeofenceZoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import com.example.domain.model.PlaybackState

private const val PLAYBACK_STEP_MS = 500L

@HiltViewModel
class MapViewModel @Inject constructor(
    private val observeRecentLocations: ObserveRecentLocationsUseCase,
    private val observeGeofenceZones: ObserveGeofenceZonesUseCase,
    private val observeRecentGeofenceEvents: ObserveRecentGeofenceEventsUseCase,
    private val saveGeofenceZone: SaveGeofenceZoneUseCase,
    private val deleteGeofenceZone: DeleteGeofenceZoneUseCase,
    private val trackingRepository: TrackingRepository
) : ViewModel() {

    private val _playbackState = MutableStateFlow(PlaybackState())
    val showAddZoneDialog = MutableStateFlow(false)

    val uiState: StateFlow<MapUiState> = combine(
        observeRecentLocations(),
        _playbackState,
        observeGeofenceZones(),
        observeRecentGeofenceEvents(),
        trackingRepository.isTracking
    ) { records, playback, zones, events, isTracking ->
        val allPoints = records.map { GeoPoint(it.latitude, it.longitude) }
        val visiblePoints = if (playback.isRunning && allPoints.isNotEmpty()) {
            allPoints.take(playback.index + 1)
        } else {
            allPoints
        }
        MapUiState(
            currentLocation = allPoints.lastOrNull(),
            trackPoints = visiblePoints,
            totalTrackCount = allPoints.size,
            isPlaybackRunning = playback.isRunning,
            playbackIndex = playback.index,
            geofenceZones = zones,
            recentGeofenceEvents = events,
            isTracking = isTracking
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    private var playbackJob: Job? = null

    fun toggleTracking() = trackingRepository.toggleTracking()

    fun startPlayback() {
        playbackJob?.cancel()
        val totalPoints = uiState.value.totalTrackCount
        if (totalPoints < 2) return
        playbackJob = viewModelScope.launch {
            for (i in 0 until totalPoints) {
                _playbackState.value = PlaybackState(isRunning = true, index = i)
                delay(PLAYBACK_STEP_MS)
            }
            _playbackState.value = PlaybackState(isRunning = false, index = totalPoints - 1)
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        _playbackState.value = PlaybackState(isRunning = false, index = 0)
    }

    fun onAddZoneClick() {
        showAddZoneDialog.value = true
    }

    fun onDismissAddZone() {
        showAddZoneDialog.value = false
    }

    fun saveZone(name: String, centerLat: Double, centerLng: Double, radiusMeters: Double) {
        viewModelScope.launch {
            saveGeofenceZone(
                GeofenceZone(
                    name = name,
                    centerLat = centerLat,
                    centerLng = centerLng,
                    radiusMeters = radiusMeters
                )
            )
            showAddZoneDialog.value = false
        }
    }

    fun deleteZone(zoneId: Long) {
        viewModelScope.launch { deleteGeofenceZone(zoneId) }
    }
}