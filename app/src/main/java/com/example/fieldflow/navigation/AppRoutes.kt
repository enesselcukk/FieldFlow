package com.example.fieldflow.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface FieldFlowRoute : NavKey

@Serializable
internal data object SplashRoute : FieldFlowRoute

@Serializable
internal data object ScanRoute : FieldFlowRoute

@Serializable
internal data class ActivationRoute(
    val name: String,
    val surname: String,
) : FieldFlowRoute

@Serializable
internal data object BiometricRoute : FieldFlowRoute

@Serializable
internal data object HomeRoute : FieldFlowRoute

@Serializable
internal data object MapRoute : FieldFlowRoute

@Serializable
internal data object EventLogRoute : FieldFlowRoute

@Serializable
internal data object SettingsRoute : FieldFlowRoute

@Serializable
internal data object NotificationListRoute : FieldFlowRoute

@Serializable
internal data class NotificationDetailRoute(
    val type: String,
    val timestamp: Long,
    val extraArg: String? = null,
) : FieldFlowRoute
