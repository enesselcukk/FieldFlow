package com.example.fieldflow.tracking

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.repository.TrackingRepository
import com.example.fieldflow.service.LocationForegroundService
import com.example.utils.permissions.canPostNotifications
import com.example.utils.permissions.hasForegroundLocationPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TrackingRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : TrackingRepository {

    override val isTracking: StateFlow<Boolean> = LocationForegroundService.isRunningFlow

    override fun startTracking() {
        if (isTracking.value) return
        if (!context.hasForegroundLocationPermission() || !context.canPostNotifications()) return
        val intent = Intent(context, LocationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun stopTracking() {
        if (!isTracking.value) return
        context.stopService(Intent(context, LocationForegroundService::class.java))
    }

    override fun toggleTracking() {
        if (isTracking.value) stopTracking() else startTracking()
    }
}
