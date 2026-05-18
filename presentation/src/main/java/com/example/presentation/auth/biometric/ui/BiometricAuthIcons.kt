package com.example.presentation.auth.biometric.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.presentation.auth.biometric.model.BiometricInteractiveUiState

internal fun biometricHeroIcon(interactive: BiometricInteractiveUiState): ImageVector =
    when (interactive) {
        BiometricInteractiveUiState.BiometricReady,
        is BiometricInteractiveUiState.NeedsEnrollment,
            -> Icons.Outlined.Fingerprint

        BiometricInteractiveUiState.DeviceCredentialOnly -> Icons.Outlined.Lock

        is BiometricInteractiveUiState.HardwareTemporarilyUnavailable ->
            Icons.Outlined.WarningAmber

        BiometricInteractiveUiState.SetupSecurityInSettings -> Icons.Outlined.Security

        is BiometricInteractiveUiState.LegacyBiometricUnsupported ->
            if (interactive.canUseDeviceCredential) Icons.Outlined.Lock else Icons.Outlined.Security
    }
