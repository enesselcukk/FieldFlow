package com.example.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface TrackingRepository {
    val isTracking: StateFlow<Boolean>
    fun startTracking()
    fun stopTracking()
    fun toggleTracking()
}
