package com.example.domain.usecase.geofence

import com.example.domain.model.GeofenceEvent
import com.example.domain.repository.GeofenceRepository
import javax.inject.Inject

class SaveGeofenceEventUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    suspend operator fun invoke(event: GeofenceEvent) = repository.saveEvent(event)
}
