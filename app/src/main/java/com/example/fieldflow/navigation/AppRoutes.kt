package com.example.fieldflow.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ScanRoute : NavKey

@Serializable
data object SplashRoute : NavKey

@Serializable
data class ActivationRoute(
    val name: String,
    val surname: String
) : NavKey

@Serializable
data object BiometricRoute : NavKey

@Serializable
data object HomeRoute : NavKey

@Serializable
data object MapRoute : NavKey

@Serializable
data object EventLogRoute : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data class NotificationDetailRoute(
    val type: String,
    val timestamp: Long,
    val extraArg: String? = null
) : NavKey

@Serializable
data object NotificationListRoute : NavKey
