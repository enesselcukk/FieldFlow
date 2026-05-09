package com.example.domain.model

data class EventRecord(
    val id: Long = 0,
    val timestamp: Long,
    val type: EventType,
    val detail: String = "",
    val note: String = ""
) {
    enum class EventType {
        GEOFENCE_EXIT,
        GEOFENCE_ENTER,
        INTERNET_LOST,
        INTERNET_RESTORED,
        LOCATION_SERVICE_DISABLED,
        LOCATION_SERVICE_ENABLED
    }
}
