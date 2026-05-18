package com.example.domain.geofence

import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import com.example.domain.model.ZoneGeofenceRuntimeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeofenceTransitionEngineTest {

    private val engine = GeofenceTransitionEngine()

    private val zone = GeofenceZone(
        id = 1L,
        name = "A",
        centerLat = 0.0,
        centerLng = 0.0,
        radiusMeters = 100.0,
    )

    @Test
    fun firstSampleProducesStateButNoTransition() {
        val r = engine.evaluate(
            listOf(zone),
            emptyMap(),
            latitude = 0.0,
            longitude = 0.0,
        )
        assertTrue(r.transitions.isEmpty())
        assertEquals(true, r.states[1L]?.logicalInside)
    }

    @Test
    fun exitRequiresConfirmationSamples() {
        val inside = mapOf(1L to ZoneGeofenceRuntimeState(logicalInside = true, 0, 0))
        val farLat = 0.002
        val farLng = 0.0
        val r1 = engine.evaluate(listOf(zone), inside, farLat, farLng)
        assertTrue(r1.states[1L]!!.logicalInside)
        assertTrue(r1.transitions.isEmpty())
        val r2 = engine.evaluate(listOf(zone), r1.states, farLat, farLng)
        assertEquals(false, r2.states[1L]!!.logicalInside)
        assertEquals(1, r2.transitions.size)
        assertEquals(GeofenceEvent.EventType.EXIT, r2.transitions[0].type)
    }

    @Test
    fun enterFromOutsideRequiresConfirmationSamples() {
        val outside = mapOf(
            1L to ZoneGeofenceRuntimeState(logicalInside = false, 0, enterStreak = 0),
        )
        val center = 0.0 to 0.0
        val r1 = engine.evaluate(listOf(zone), outside, center.first, center.second)
        assertEquals(false, r1.states[1L]!!.logicalInside)
        assertTrue(r1.transitions.isEmpty())
        val r2 = engine.evaluate(listOf(zone), r1.states, center.first, center.second)
        assertEquals(true, r2.states[1L]!!.logicalInside)
        assertEquals(1, r2.transitions.size)
        assertEquals(GeofenceEvent.EventType.ENTER, r2.transitions[0].type)
    }
}
