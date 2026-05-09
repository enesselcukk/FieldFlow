package com.example.domain.usecase

import com.example.domain.model.GeofenceEvent
import com.example.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecentGeofenceEventsUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    operator fun invoke(): Flow<List<GeofenceEvent>> = repository.observeRecentEvents()
}
