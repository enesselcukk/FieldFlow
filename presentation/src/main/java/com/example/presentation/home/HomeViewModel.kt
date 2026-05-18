package com.example.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.RuntimePermissions
import com.example.domain.repository.StatusRepository
import com.example.domain.repository.TrackingRepository
import com.example.presentation.home.model.HomeUiState
import com.example.utils.permissions.snapshotRuntimePermissions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val trackingRepository: TrackingRepository,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val appContext = context.applicationContext
    private val bootstrapPermissions = appContext.snapshotRuntimePermissions()
    private val runtimePermissionsFlow = MutableStateFlow(bootstrapPermissions)

    val uiState: StateFlow<HomeUiState> = combine(
        statusRepository.observeConnectivity(),
        statusRepository.observeLocationEnabled(),
        statusRepository.observeBatteryLevel(),
        runtimePermissionsFlow,
        trackingRepository.isTracking,
    ) { isOnline, isLocationEnabled, batteryLevel, runtimePerms, isTracking ->
        HomeUiState(
            isOnline = isOnline,
            isLocationEnabled = isLocationEnabled,
            hasNotificationPermission = runtimePerms.hasNotificationPermission,
            hasForegroundLocationPermission = runtimePerms.hasForegroundLocationPermission,
            hasBackgroundLocationPermission = runtimePerms.hasBackgroundLocationPermission,
            batteryLevel = batteryLevel,
            isTracking = isTracking,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = bootstrapPermissions.toPartialHomeUiState(),
    )

    fun toggleTracking() {
        trackingRepository.toggleTracking()
    }

    fun onForegroundLocationAccessChanged(granted: Boolean) {
        if (!granted && trackingRepository.isTracking.value) {
            trackingRepository.stopTracking()
        }
    }

    fun refreshRuntimePermissions() {
        runtimePermissionsFlow.update { appContext.snapshotRuntimePermissions() }
    }

    private fun RuntimePermissions.toPartialHomeUiState() = HomeUiState(
        hasNotificationPermission = hasNotificationPermission,
        hasForegroundLocationPermission = hasForegroundLocationPermission,
        hasBackgroundLocationPermission = hasBackgroundLocationPermission,
    )
}
