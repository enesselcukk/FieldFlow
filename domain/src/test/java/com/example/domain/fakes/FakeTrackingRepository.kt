package com.example.domain.fakes

import com.example.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTrackingRepository(
    tracking: Boolean = false
) : TrackingRepository {

    private val state = MutableStateFlow(tracking)
    override val isTracking: StateFlow<Boolean> = state.asStateFlow()

    var starts = 0
        private set
    var stops = 0
        private set

    override fun startTracking() {
        starts++
        state.value = true
    }

    override fun stopTracking() {
        stops++
        state.value = false
    }

    override fun toggleTracking() {
        state.value = !state.value
    }
}
