package com.example.fieldflow.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface FieldFlowRoute : NavKey

@Serializable
data object SplashRoute : FieldFlowRoute

@Serializable
data object ScanRoute : FieldFlowRoute

@Serializable
data class ActivationRoute(
    val name: String,
    val surname: String,
) : FieldFlowRoute

@Serializable
data object BiometricRoute : FieldFlowRoute

@Serializable
data object HomeRoute : FieldFlowRoute

@Serializable
data object MapRoute : FieldFlowRoute

@Serializable
data object EventLogRoute : FieldFlowRoute

@Serializable
data object SettingsRoute : FieldFlowRoute

@Serializable
data object NotificationListRoute : FieldFlowRoute

@Serializable
data class NotificationDetailRoute(
    val type: String,
    val timestamp: Long,
    val extraArg: String? = null,
) : FieldFlowRoute
