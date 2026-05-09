package com.example.domain.usecase

import com.example.domain.model.LocationRecord
import com.example.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val HOURS_24_MS = 24 * 60 * 60 * 1000L

class ObserveRecentLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<LocationRecord>> =
        repository.getLocationsAfter(System.currentTimeMillis() - HOURS_24_MS)
}
