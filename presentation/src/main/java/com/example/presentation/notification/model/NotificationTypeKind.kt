package com.example.presentation.notification.model

import com.example.domain.constants.NOTIF_TYPE_BATTERY
import com.example.domain.constants.NOTIF_TYPE_GEOFENCE
import com.example.domain.constants.NOTIF_TYPE_INTERNET
import com.example.domain.constants.NOTIF_TYPE_LOCATION

internal enum class NotificationTypeKind {
    Geofence,
    Internet,
    Location,
    Battery,
    Unknown,
    ;

    companion object {
        fun from(type: String): NotificationTypeKind = when (type) {
            NOTIF_TYPE_GEOFENCE -> Geofence
            NOTIF_TYPE_INTERNET -> Internet
            NOTIF_TYPE_LOCATION -> Location
            NOTIF_TYPE_BATTERY -> Battery
            else -> Unknown
        }
    }
}

internal enum class NotificationDetailFooterAction {
    EventLog,
    Home,
    None,
}
