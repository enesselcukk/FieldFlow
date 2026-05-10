package com.example.domain.usecase.geofence

import com.example.domain.model.GeofenceZone
import com.example.domain.repository.GeofenceRepository
import javax.inject.Inject

class GetAllGeofenceZonesUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    suspend operator fun invoke(): List<GeofenceZone> = repository.getAllZones()
}
