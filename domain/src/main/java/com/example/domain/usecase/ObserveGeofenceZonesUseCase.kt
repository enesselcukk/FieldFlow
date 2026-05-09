package com.example.domain.usecase

import com.example.domain.model.GeofenceZone
import com.example.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGeofenceZonesUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    operator fun invoke(): Flow<List<GeofenceZone>> = repository.observeAllZones()
}
