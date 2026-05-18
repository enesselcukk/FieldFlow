package com.example.presentation.auth.biometric.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.presentation.R
import com.example.presentation.auth.biometric.model.BiometricInteractiveUiState

@Composable
internal fun biometricDescriptionForInteractive(state: BiometricInteractiveUiState): String {
    return when (state) {
        BiometricInteractiveUiState.BiometricReady -> stringResource(R.string.biometric_description)
        is BiometricInteractiveUiState.LegacyBiometricUnsupported ->
            if (state.canUseDeviceCredential) {
                stringResource(R.string.biometric_device_credential_only_description)
            } else {
                stringResource(R.string.biometric_setup_security_description)
            }

        is BiometricInteractiveUiState.NeedsEnrollment ->
            stringResource(R.string.biometric_needs_enrollment_description)

        BiometricInteractiveUiState.DeviceCredentialOnly ->
            stringResource(R.string.biometric_device_credential_only_description)

        is BiometricInteractiveUiState.HardwareTemporarilyUnavailable ->
            stringResource(R.string.biometric_hw_unavailable_description)

        BiometricInteractiveUiState.SetupSecurityInSettings ->
            stringResource(R.string.biometric_setup_security_description)
    }
}
