package com.example.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelRecordsTest {

    private val anyType = EventRecord.EventType.INTERNET_LOST

    @Test
    fun eventSyncedWhenSyncedAtSet() {
        assertTrue(EventRecord(timestamp = 0L, type = anyType, syncedAt = 10L).isSynced)
    }

    @Test
    fun eventNotSyncedWhenSyncedAtNull() {
        assertFalse(EventRecord(timestamp = 0L, type = anyType, syncedAt = null).isSynced)
    }

    @Test
    fun eventOfflineDurationFromSyncedMinusTimestamp() {
        assertEquals(
            20L,
            EventRecord(timestamp = 5L, type = anyType, syncedAt = 25L).offlineDurationMs
        )
    }

    @Test
    fun eventOfflineDurationNullWhenNotSynced() {
        assertNull(EventRecord(timestamp = 5L, type = anyType, syncedAt = null).offlineDurationMs)
    }

    @Test
    fun locationSyncedWhenSyncedAtSet() {
        assertTrue(
            LocationRecord(
                latitude = 0.0,
                longitude = 0.0,
                timestamp = 0L,
                syncedAt = 1L
            ).isSynced
        )
    }

    @Test
    fun locationOfflineDurationDerived() {
        assertEquals(
            100L,
            LocationRecord(
                latitude = 0.0,
                longitude = 0.0,
                timestamp = 10L,
                syncedAt = 110L
            ).offlineDurationMs
        )
    }
}
