package com.example.fieldflow.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import com.example.domain.geofence.GeofenceTransitionEngine
import com.example.domain.model.EventRecord
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.LocationRecord
import com.example.domain.model.ZoneGeofenceRuntimeState
import com.example.domain.repository.SettingsRepository
import com.example.domain.usecase.event.SaveEventUseCase
import com.example.domain.usecase.geofence.GetAllGeofenceZonesUseCase
import com.example.domain.usecase.geofence.SaveGeofenceEventUseCase
import com.example.domain.usecase.location.SaveLocationUseCase
import com.example.fieldflow.constants.NOTIFICATION_ID_TRACKING
import com.example.fieldflow.notification.NotificationHelper
import com.example.utils.permissions.canPostNotifications
import com.example.utils.permissions.hasForegroundLocationPermission
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@AndroidEntryPoint
internal class LocationForegroundService : Service() {

    @Inject lateinit var saveLocation: SaveLocationUseCase
    @Inject lateinit var getAllGeofenceZones: GetAllGeofenceZonesUseCase
    @Inject lateinit var saveGeofenceEvent: SaveGeofenceEventUseCase
    @Inject lateinit var saveEvent: SaveEventUseCase
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var deviceSignalsMonitor: TrackingDeviceSignalsMonitor
    @Inject lateinit var geofenceEngine: GeofenceTransitionEngine

    private lateinit var locationSession: FusedLocationTrackingSession
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var intervalJob: Job? = null

    private val zoneStates = mutableMapOf<Long, ZoneGeofenceRuntimeState>()
    private val zoneStatesMutex = Mutex()

    override fun onCreate() {
        super.onCreate()
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        locationSession = FusedLocationTrackingSession(
            context = this,
            fusedClient = fusedClient,
            coroutineScope = serviceScope,
            onStartFailure = { stopSelf() },
        )
        deviceSignalsMonitor.start(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasForegroundLocationPermission() || !canPostNotifications()) {
            stopSelf()
            return START_NOT_STICKY
        }
        return try {
            notificationHelper.createChannels()
            startForeground(
                NOTIFICATION_ID_TRACKING,
                notificationHelper.buildTrackingNotification(),
            )
            setRunning(true)
            startObservingInterval()
            START_STICKY
        } catch (_: SecurityException) {
            stopSelf()
            START_NOT_STICKY
        }
    }

    private fun startObservingInterval() {
        intervalJob?.cancel()
        intervalJob = serviceScope.launch {
            settingsRepository.preferences
                .map { it.locationIntervalSeconds * 1000L }
                .distinctUntilChanged()
                .collect { intervalMs ->
                    locationSession.start(intervalMs) { location ->
                        saveLocationSample(location)
                        processGeofences(location)
                    }
                }
        }
    }

    override fun onDestroy() {
        intervalJob?.cancel()
        locationSession.stop()
        serviceScope.cancel()
        setRunning(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun saveLocationSample(location: Location) {
        saveLocation(
            LocationRecord(
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = location.time.takeIf { it > 0L }
                    ?: System.currentTimeMillis(),
            ),
        )
    }

    private suspend fun processGeofences(location: Location) {
        val zones = getAllGeofenceZones()
        val now = System.currentTimeMillis()
        val transitions = zoneStatesMutex.withLock {
            val result = geofenceEngine.evaluate(
                zones,
                zoneStates.toMap(),
                location.latitude,
                location.longitude,
            )
            zoneStates.clear()
            zoneStates.putAll(result.states)
            result.transitions
        }
        for (t in transitions) {
            saveGeofenceEvent(
                GeofenceEvent(
                    zoneId = t.zoneId,
                    zoneName = t.zoneName,
                    timestamp = now,
                    eventType = t.type,
                ),
            )
            saveEvent(
                EventRecord(
                    timestamp = now,
                    type = when (t.type) {
                        GeofenceEvent.EventType.EXIT -> EventRecord.EventType.GEOFENCE_EXIT
                        GeofenceEvent.EventType.ENTER -> EventRecord.EventType.GEOFENCE_ENTER
                    },
                    detail = t.zoneName,
                ),
            )
            if (t.type == GeofenceEvent.EventType.EXIT) {
                notificationHelper.sendGeofenceExitAlert(t.zoneId.toInt(), t.zoneName)
            }
        }
    }

    companion object {
        private val _isRunning = MutableStateFlow(false)
        val isRunningFlow: StateFlow<Boolean> = _isRunning

        internal fun setRunning(value: Boolean) {
            _isRunning.value = value
        }
    }
}
