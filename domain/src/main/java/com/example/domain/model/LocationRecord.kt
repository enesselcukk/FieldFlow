package com.example.domain.model

data class LocationRecord(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val syncedAt: Long? = null
) {
    val isSynced: Boolean get() = syncedAt != null
    val offlineDurationMs: Long? get() = syncedAt?.let { it - timestamp }
}
