package com.example.fieldflow.tracking

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.repository.TrackingRepository
import com.example.fieldflow.service.LocationForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TrackingRepository {

    private val _isTracking = MutableStateFlow(LocationForegroundService.isRunning)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    override fun startTracking() {
        if (_isTracking.value) return
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            putExtra(
                LocationForegroundService.EXTRA_INTERVAL_MS,
                LocationForegroundService.DEFAULT_INTERVAL_MS
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _isTracking.value = true
    }

    override fun stopTracking() {
        if (!_isTracking.value) return
        context.stopService(Intent(context, LocationForegroundService::class.java))
        _isTracking.value = false
    }

    override fun toggleTracking() {
        if (_isTracking.value) stopTracking() else startTracking()
    }
}
