package com.example.domain.model

data class GeofenceEvent(
    val id: Long = 0,
    val zoneId: Long,
    val zoneName: String,
    val timestamp: Long,
    val eventType: EventType
) {
    enum class EventType { ENTER, EXIT }
}
