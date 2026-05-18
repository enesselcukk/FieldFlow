package com.example.domain.geofence

import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceTickResult
import com.example.domain.model.GeofenceTransition
import com.example.domain.model.GeofenceZone
import com.example.domain.model.ZoneGeofenceRuntimeState
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class GeofenceTransitionEngine @Inject constructor() {

    fun evaluate(
        zones: List<GeofenceZone>,
        states: Map<Long, ZoneGeofenceRuntimeState>,
        latitude: Double,
        longitude: Double,
    ): GeofenceTickResult {
        val newStates = states.toMutableMap()
        val transitions = mutableListOf<GeofenceTransition>()
        for (zone in zones) {
            val distanceMeters = distanceMeters(latitude, longitude, zone.centerLat, zone.centerLng)
            val radius = zone.radiusMeters
            val prevState = newStates[zone.id]
            val newState = when {
                prevState == null -> {
                    val inside = distanceMeters <= radius
                    ZoneGeofenceRuntimeState(logicalInside = inside, 0, 0)
                }
                prevState.logicalInside -> {
                    val wantsExit = distanceMeters > radius + EXIT_HYSTERESIS_METERS
                    val exitStreak = if (wantsExit) prevState.exitStreak + 1 else 0
                    val stillInside = exitStreak < CONFIRMATION_SAMPLES
                    ZoneGeofenceRuntimeState(
                        logicalInside = stillInside,
                        exitStreak = if (stillInside) exitStreak else 0,
                        enterStreak = 0,
                    )
                }
                else -> {
                    val enterTh = enterThresholdMeters(radius)
                    val wantsEnter = distanceMeters <= enterTh
                    val enterStreak = if (wantsEnter) prevState.enterStreak + 1 else 0
                    val nowInside = enterStreak >= CONFIRMATION_SAMPLES
                    ZoneGeofenceRuntimeState(
                        logicalInside = nowInside,
                        exitStreak = 0,
                        enterStreak = if (nowInside) 0 else enterStreak,
                    )
                }
            }
            newStates[zone.id] = newState
            if (prevState != null) {
                val wasInside = prevState.logicalInside
                val nowInside = newState.logicalInside
                when {
                    wasInside && !nowInside ->
                        transitions.add(
                            GeofenceTransition(zone.id, zone.name, GeofenceEvent.EventType.EXIT),
                        )
                    !wasInside && nowInside ->
                        transitions.add(
                            GeofenceTransition(zone.id, zone.name, GeofenceEvent.EventType.ENTER),
                        )
                }
            }
        }
        return GeofenceTickResult(newStates, transitions)
    }

    private fun enterThresholdMeters(radius: Double): Double {
        val margin = min(
            ENTER_HYSTERESIS_METERS,
            min(radius * 0.15, radius - 5.0).coerceAtLeast(0.0),
        )
        return (radius - margin).coerceAtLeast(radius * 0.55)
    }

    private companion object {
        const val EXIT_HYSTERESIS_METERS = 25.0
        const val ENTER_HYSTERESIS_METERS = 15.0
        const val CONFIRMATION_SAMPLES = 2
        const val EARTH_RADIUS_METERS = 6371000.0

        fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val rLat1 = Math.toRadians(lat1)
            val rLat2 = Math.toRadians(lat2)
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(rLat1) * cos(rLat2) * sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_METERS * c
        }
    }
}
