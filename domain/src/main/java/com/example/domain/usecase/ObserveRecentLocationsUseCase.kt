package com.example.domain.usecase

import com.example.domain.constants.DAY_MS
import com.example.domain.model.LocationRecord
import com.example.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecentLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<LocationRecord>> =
        repository.getLocationsAfter(System.currentTimeMillis() - DAY_MS)
}
