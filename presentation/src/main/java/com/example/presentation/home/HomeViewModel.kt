package com.example.presentation.home

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.StatusRepository
import com.example.domain.repository.TrackingRepository
import com.example.domain.usecase.StartTrackingUseCase
import com.example.domain.usecase.StopTrackingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val trackingRepository: TrackingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _runtimePermissions = MutableStateFlow(
        RuntimePermissions(
            isBatteryOptimizationIgnored = checkBatteryOptimization(),
            hasNotificationPermission = checkNotificationPermission()
        )
    )

    val uiState: StateFlow<HomeUiState> = combine(
        statusRepository.observeConnectivity(),
        statusRepository.observeLocationEnabled(),
        statusRepository.observeBatteryLevel(),
        _runtimePermissions,
        trackingRepository.isTracking
    ) { isOnline, isLocationEnabled, batteryLevel, runtimePerms, isTracking ->
        HomeUiState(
            isOnline = isOnline,
            isLocationEnabled = isLocationEnabled,
            isBatteryOptimizationIgnored = runtimePerms.isBatteryOptimizationIgnored,
            hasNotificationPermission = runtimePerms.hasNotificationPermission,
            batteryLevel = batteryLevel,
            isTracking = isTracking
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun toggleTracking() = trackingRepository.toggleTracking()

    fun refreshRuntimePermissions() {
        _runtimePermissions.update {
            RuntimePermissions(
                isBatteryOptimizationIgnored = checkBatteryOptimization(),
                hasNotificationPermission = checkNotificationPermission()
            )
        }
    }

    private fun checkBatteryOptimization(): Boolean {
        val pm = context.getSystemService(PowerManager::class.java)
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

private data class RuntimePermissions(
    val isBatteryOptimizationIgnored: Boolean,
    val hasNotificationPermission: Boolean
)
