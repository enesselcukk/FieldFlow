package com.example.domain.usecase

import com.example.domain.repository.GeofenceRepository
import javax.inject.Inject

class DeleteGeofenceZoneUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    suspend operator fun invoke(zoneId: Long) = repository.deleteZone(zoneId)
}
