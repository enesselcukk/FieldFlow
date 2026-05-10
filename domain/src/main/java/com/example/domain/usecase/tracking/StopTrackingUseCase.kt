package com.example.domain.usecase.tracking

import com.example.domain.repository.TrackingRepository
import javax.inject.Inject

class StopTrackingUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository
) {
    operator fun invoke() = trackingRepository.stopTracking()
}
