package com.example.fieldflow

import com.example.fieldflow.activation.ACTIVATION_GCM_AUTH_TAG_BYTES
import com.example.fieldflow.activation.EmbeddedActivationPayload
import com.example.fieldflow.constants.CHANNEL_GEOFENCE
import com.example.fieldflow.constants.CHANNEL_SYSTEM
import com.example.fieldflow.constants.CHANNEL_TRACKING
import com.example.fieldflow.constants.NAV_HOME
import com.example.fieldflow.constants.NAV_NOTIFICATION_DETAIL
import com.example.fieldflow.constants.NOTIFICATION_ID_GEOFENCE_BASE
import com.example.fieldflow.constants.NOTIFICATION_ID_TRACKING
import com.example.fieldflow.constants.SYNC_BACKOFF_SECONDS
import com.example.fieldflow.constants.SYNC_MAX_RETRY_COUNT
import com.example.fieldflow.constants.SYNC_PERIODIC_INTERVAL_HOURS
import com.example.fieldflow.constants.WORK_NAME_ONE_TIME
import com.example.fieldflow.constants.WORK_NAME_PERIODIC
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class AppConstantsTest {

    @Test
    fun activationDerivedTagBytes() {
        assertEquals(16, ACTIVATION_GCM_AUTH_TAG_BYTES)
    }

    @Test
    fun embeddedPayloadCipherIsValidBase64() {
        val decoded = Base64.getDecoder().decode(EmbeddedActivationPayload.CIPHER_TEXT_B64)
        assertTrue(decoded.isNotEmpty())
        assertTrue(EmbeddedActivationPayload.KEY_MATERIAL_LABEL.isNotBlank())
    }

    @Test
    fun syncWorkerNamesAndPolicy() {
        assertTrue(WORK_NAME_ONE_TIME.isNotBlank())
        assertTrue(WORK_NAME_PERIODIC.isNotBlank())
        assertEquals(3, SYNC_MAX_RETRY_COUNT)
        assertEquals(30L, SYNC_BACKOFF_SECONDS)
        assertEquals(6L, SYNC_PERIODIC_INTERVAL_HOURS)
    }

    @Test
    fun notificationChannelAndIdsDistinct() {
        assertTrue(CHANNEL_TRACKING != CHANNEL_GEOFENCE)
        assertTrue(CHANNEL_TRACKING != CHANNEL_SYSTEM)
        assertTrue(NOTIFICATION_ID_TRACKING < NOTIFICATION_ID_GEOFENCE_BASE)
        assertEquals("nav_home", NAV_HOME)
        assertEquals("nav_notification_detail", NAV_NOTIFICATION_DETAIL)
    }
}
