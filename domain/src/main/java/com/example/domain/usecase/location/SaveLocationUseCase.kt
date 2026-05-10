package com.example.domain.usecase.location

import com.example.domain.constants.SYNCED_LOCATION_RETENTION_MS
import com.example.domain.constants.UNSYNCED_LOCATION_RETENTION_MS
import com.example.domain.model.LocationRecord
import com.example.domain.repository.LocationRepository
import javax.inject.Inject

class SaveLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(record: LocationRecord) {
        repository.insertLocation(record)
        val now = System.currentTimeMillis()
        repository.deleteSyncedOlderThan(now - SYNCED_LOCATION_RETENTION_MS)
        repository.deleteOlderThan(now - UNSYNCED_LOCATION_RETENTION_MS)
    }
}
