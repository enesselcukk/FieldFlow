package com.example.domain.usecase

import com.example.domain.repository.TrackingRepository
import javax.inject.Inject

class StartTrackingUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository
) {
    operator fun invoke() = trackingRepository.startTracking()
}
