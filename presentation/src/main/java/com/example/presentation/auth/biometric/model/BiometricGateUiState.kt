package com.example.presentation.auth.biometric.model

sealed interface BiometricGateUiState {
    data object Loading : BiometricGateUiState

    data object BiometricReady : BiometricGateUiState

    data class NeedsEnrollment(
        val canUseDeviceCredential: Boolean,
    ) : BiometricGateUiState

    data object DeviceCredentialOnly : BiometricGateUiState

    data class HardwareTemporarilyUnavailable(
        val canUseDeviceCredential: Boolean,
    ) : BiometricGateUiState

    data object SetupSecurityInSettings : BiometricGateUiState

    data class LegacyBiometricUnsupported(
        val canUseDeviceCredential: Boolean,
    ) : BiometricGateUiState

    data object NoHostActivity : BiometricGateUiState
}
