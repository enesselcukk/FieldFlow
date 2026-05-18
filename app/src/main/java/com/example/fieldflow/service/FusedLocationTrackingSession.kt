package com.example.fieldflow.service

import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.utils.permissions.hasForegroundLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class FusedLocationTrackingSession(
    private val context: Context,
    private val fusedClient: FusedLocationProviderClient,
    private val coroutineScope: CoroutineScope,
    private val onStartFailure: () -> Unit,
) {

    private var locationCallback: LocationCallback? = null

    fun stop() {
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    @Suppress("MissingPermission")
    fun start(intervalMs: Long, onLocation: suspend (Location) -> Unit) {
        stop()
        if (!context.hasForegroundLocationPermission()) {
            onStartFailure()
            return
        }
        val request = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    coroutineScope.launch {
                        onLocation(location)
                    }
                }
            }
        }
        try {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper(),
            )
        } catch (_: SecurityException) {
            stop()
            onStartFailure()
        }
    }
}
