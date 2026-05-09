package com.example.domain.usecase

import com.example.domain.model.LocationRecord
import com.example.domain.repository.LocationRepository
import javax.inject.Inject

private const val RETENTION_MS = 24 * 60 * 60 * 1000L

class SaveLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(record: LocationRecord) {
        repository.insertLocation(record)
        repository.deleteOlderThan(System.currentTimeMillis() - RETENTION_MS)
    }
}
