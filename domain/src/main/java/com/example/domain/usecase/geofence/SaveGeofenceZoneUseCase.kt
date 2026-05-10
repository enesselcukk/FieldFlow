package com.example.domain.usecase.geofence

import com.example.domain.model.GeofenceZone
import com.example.domain.repository.GeofenceRepository
import javax.inject.Inject

class SaveGeofenceZoneUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    suspend operator fun invoke(zone: GeofenceZone) = repository.saveZone(zone)
}
