package com.example.fieldflow.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.domain.model.EventRecord
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.LocationRecord
import com.example.domain.model.ZoneGeofenceRuntimeState
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.StatusRepository
import com.example.domain.usecase.event.SaveEventUseCase
import com.example.domain.usecase.geofence.GetAllGeofenceZonesUseCase
import com.example.domain.usecase.geofence.SaveGeofenceEventUseCase
import com.example.domain.usecase.location.SaveLocationUseCase
import com.example.fieldflow.constants.BATTERY_LOW_THRESHOLD
import com.example.fieldflow.constants.NOTIFICATION_ID_TRACKING
import com.example.fieldflow.notification.NotificationHelper
import com.example.fieldflow.sync.SyncWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.math.min
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
internal class LocationForegroundService : Service() {

    @Inject lateinit var saveLocation: SaveLocationUseCase
    @Inject lateinit var getAllGeofenceZones: GetAllGeofenceZonesUseCase
    @Inject lateinit var saveGeofenceEvent: SaveGeofenceEventUseCase
    @Inject lateinit var saveEvent: SaveEventUseCase
    @Inject lateinit var statusRepository: StatusRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var settingsRepository: SettingsRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var intervalJob: Job? = null

    private val zoneStates = mutableMapOf<Long, ZoneGeofenceRuntimeState>()
    private val zoneStatesMutex = Mutex()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setRunning(true)
        startMonitoringSystemEvents()
    }

    @Suppress("MissingPermission")
    private fun startMonitoringSystemEvents() {
        serviceScope.launch {
            var isFirst = true
            statusRepository.observeConnectivity().collect { isOnline ->
                if (isFirst) { isFirst = false; return@collect }
                saveEvent(
                    EventRecord(
                        timestamp = System.currentTimeMillis(),
                        type = if (isOnline) EventRecord.EventType.INTERNET_RESTORED
                               else EventRecord.EventType.INTERNET_LOST
                    )
                )
                if (isOnline) {
                    notificationHelper.cancelInternetLostAlert()
                    SyncWorker.schedule(applicationContext)
                } else {
                    notificationHelper.sendInternetLostAlert()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            serviceScope.launch {
                var isFirst = true
                statusRepository.observeLocationEnabled().collect { isEnabled ->
                    if (isFirst) { isFirst = false; return@collect }
                    saveEvent(
                        EventRecord(
                            timestamp = System.currentTimeMillis(),
                            type = if (isEnabled) EventRecord.EventType.LOCATION_SERVICE_ENABLED
                                   else EventRecord.EventType.LOCATION_SERVICE_DISABLED
                        )
                    )
                    if (isEnabled) {
                        notificationHelper.cancelLocationServiceDisabledAlert()
                    } else {
                        notificationHelper.sendLocationServiceDisabledAlert()
                    }
                }
            }
        }
        serviceScope.launch {
            var isFirst = true
            statusRepository.observeBatteryLevel().collect { level ->
                if (isFirst) { isFirst = false; return@collect }
                when {
                    level in 0..BATTERY_LOW_THRESHOLD ->
                        notificationHelper.sendBatteryLowAlert(level)
                    level > BATTERY_LOW_THRESHOLD ->
                        notificationHelper.cancelBatteryLowAlert()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationHelper.createChannels()
        startForeground(
            NOTIFICATION_ID_TRACKING,
            notificationHelper.buildTrackingNotification()
        )
        startObservingInterval()
        return START_STICKY
    }

    private fun startObservingInterval() {
        intervalJob?.cancel()
        intervalJob = serviceScope.launch {
            settingsRepository.preferences
                .map { it.locationIntervalSeconds * 1000L }
                .distinctUntilChanged()
                .collect { intervalMs ->
                    restartLocationUpdates(intervalMs)
                }
        }
    }

    override fun onDestroy() {
        stopLocationUpdates()
        serviceScope.cancel()
        setRunning(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun restartLocationUpdates(intervalMs: Long) {
        stopLocationUpdates()
        startLocationUpdates(intervalMs)
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    private fun startLocationUpdates(intervalMs: Long) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation) { stopSelf(); return }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    serviceScope.launch {
                        saveLocation(
                            LocationRecord(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        checkGeofences(location)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request, locationCallback!!, Looper.getMainLooper()
        )
    }

    private suspend fun checkGeofences(location: Location) {
        val zones = getAllGeofenceZones()
        val now = System.currentTimeMillis()

        for (zone in zones) {
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                zone.centerLat, zone.centerLng,
                results
            )
            val distanceMeters = results[0].toDouble()
            val radius = zone.radiusMeters
            val (prevInside, isInsideNow) = zoneStatesMutex.withLock {
                val prevState = zoneStates[zone.id]
                val newState = when {
                    prevState == null -> {
                        val inside = distanceMeters <= radius
                        ZoneGeofenceRuntimeState(logicalInside = inside, 0, 0)
                    }
                    prevState.logicalInside -> {
                        val wantsExit = distanceMeters > radius + GEOFENCE_EXIT_HYSTERESIS_METERS
                        val exitStreak = if (wantsExit) prevState.exitStreak + 1 else 0
                        val stillInside = exitStreak < GEOFENCE_CONFIRMATION_SAMPLES
                        ZoneGeofenceRuntimeState(
                            logicalInside = stillInside,
                            exitStreak = if (stillInside) exitStreak else 0,
                            enterStreak = 0
                        )
                    }
                    else -> {
                        val enterTh = enterThresholdMeters(radius)
                        val wantsEnter = distanceMeters <= enterTh
                        val enterStreak = if (wantsEnter) prevState.enterStreak + 1 else 0
                        val nowInside = enterStreak >= GEOFENCE_CONFIRMATION_SAMPLES
                        ZoneGeofenceRuntimeState(
                            logicalInside = nowInside,
                            exitStreak = 0,
                            enterStreak = if (nowInside) 0 else enterStreak
                        )
                    }
                }
                zoneStates[zone.id] = newState
                prevState?.logicalInside to newState.logicalInside
            }

            if (prevInside == null) continue

            when {
                prevInside && !isInsideNow -> {
                    saveGeofenceEvent(
                        GeofenceEvent(
                            zoneId = zone.id,
                            zoneName = zone.name,
                            timestamp = now,
                            eventType = GeofenceEvent.EventType.EXIT
                        )
                    )
                    saveEvent(
                        EventRecord(
                            timestamp = now,
                            type = EventRecord.EventType.GEOFENCE_EXIT,
                            detail = zone.name
                        )
                    )
                    
                    
                    notificationHelper.sendGeofenceExitAlert(zone.id.toInt(), zone.name)
                }
                !prevInside && isInsideNow -> {
                    saveGeofenceEvent(
                        GeofenceEvent(
                            zoneId = zone.id,
                            zoneName = zone.name,
                            timestamp = now,
                            eventType = GeofenceEvent.EventType.ENTER
                        )
                    )
                    saveEvent(
                        EventRecord(
                            timestamp = now,
                            type = EventRecord.EventType.GEOFENCE_ENTER,
                            detail = zone.name
                        )
                    )
                }
            }
        }
    }

    private fun enterThresholdMeters(radius: Double): Double {
        val margin = min(
            GEOFENCE_ENTER_HYSTERESIS_METERS,
            min(radius * 0.15, radius - 5.0).coerceAtLeast(0.0)
        )
        return (radius - margin).coerceAtLeast(radius * 0.55)
    }

    companion object {
        private const val GEOFENCE_EXIT_HYSTERESIS_METERS = 25.0
        private const val GEOFENCE_ENTER_HYSTERESIS_METERS = 15.0
        private const val GEOFENCE_CONFIRMATION_SAMPLES = 2

        private val _isRunning = MutableStateFlow(false)
        val isRunningFlow: StateFlow<Boolean> = _isRunning

        internal fun setRunning(value: Boolean) { _isRunning.value = value }
    }
}
