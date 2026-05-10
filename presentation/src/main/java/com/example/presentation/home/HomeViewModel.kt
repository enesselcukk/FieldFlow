package com.example.presentation.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

    private val _runtimePermissions = MutableStateFlow(buildRuntimePermissions())

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
            hasNotificationPermission = runtimePerms.hasNotificationPermission,
            hasFineLocationPermission = runtimePerms.hasFineLocationPermission,
            hasBackgroundLocationPermission = runtimePerms.hasBackgroundLocationPermission,
            batteryLevel = batteryLevel,
            isTracking = isTracking
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _runtimePermissions.value.let { perms ->
            HomeUiState(
                hasNotificationPermission = perms.hasNotificationPermission,
                hasFineLocationPermission = perms.hasFineLocationPermission,
                hasBackgroundLocationPermission = perms.hasBackgroundLocationPermission
            )
        }
    )

    fun toggleTracking() = trackingRepository.toggleTracking()

    fun refreshRuntimePermissions() {
        _runtimePermissions.update { buildRuntimePermissions() }
    }

    private fun buildRuntimePermissions() = RuntimePermissions(
        hasNotificationPermission = checkNotificationPermission(),
        hasFineLocationPermission = checkFineLocationPermission(),
        hasBackgroundLocationPermission = checkBackgroundLocationPermission()
    )

    private fun checkNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    private fun checkFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun checkBackgroundLocationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true
}

private data class RuntimePermissions(
    val hasNotificationPermission: Boolean,
    val hasFineLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
)
