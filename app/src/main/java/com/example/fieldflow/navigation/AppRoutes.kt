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
