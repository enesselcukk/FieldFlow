package com.example.fieldflow.navigation

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldFlowRouteSerializationTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Test
    fun activationRouteRoundTrip() {
        val original = ActivationRoute(name = "Ali", surname = "Veli")
        val wire = json.encodeToString(ActivationRoute.serializer(), original)
        assertEquals(original, json.decodeFromString(ActivationRoute.serializer(), wire))
    }

    @Test
    fun notificationDetailRoundTrip() {
        val withExtra = NotificationDetailRoute(type = "t", timestamp = 99L, extraArg = "e")
        val w1 = json.encodeToString(NotificationDetailRoute.serializer(), withExtra)
        assertEquals(withExtra, json.decodeFromString(NotificationDetailRoute.serializer(), w1))

        val minimal = NotificationDetailRoute(type = "x", timestamp = 1L)
        val w2 = json.encodeToString(NotificationDetailRoute.serializer(), minimal)
        assertEquals(minimal, json.decodeFromString(NotificationDetailRoute.serializer(), w2))
    }

    @Test
    fun sealedHomeRoundTrip() {
        val wire = json.encodeToString(FieldFlowRoute.serializer(), HomeRoute)
        assertEquals(HomeRoute, json.decodeFromString(FieldFlowRoute.serializer(), wire))
    }

    @Test
    fun sealedActivationRoundTrip() {
        val route = ActivationRoute("N", "S")
        val wire = json.encodeToString(FieldFlowRoute.serializer(), route)
        val decoded = json.decodeFromString(FieldFlowRoute.serializer(), wire)
        assertEquals(route, decoded)
    }
}
