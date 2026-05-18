package com.example.presentation.auth.biometric.model

internal sealed interface BiometricInteractiveUiState {
    data object BiometricReady : BiometricInteractiveUiState

    data class NeedsEnrollment(
        val canUseDeviceCredential: Boolean,
    ) : BiometricInteractiveUiState

    data object DeviceCredentialOnly : BiometricInteractiveUiState

    data class HardwareTemporarilyUnavailable(
        val canUseDeviceCredential: Boolean,
    ) : BiometricInteractiveUiState

    data object SetupSecurityInSettings : BiometricInteractiveUiState

    data class LegacyBiometricUnsupported(
        val canUseDeviceCredential: Boolean,
    ) : BiometricInteractiveUiState
}

internal fun BiometricGateUiState.toInteractiveOrNull(): BiometricInteractiveUiState? =
    when (this) {
        BiometricGateUiState.Loading,
        BiometricGateUiState.NoHostActivity,
        -> null

        BiometricGateUiState.BiometricReady -> BiometricInteractiveUiState.BiometricReady
        is BiometricGateUiState.NeedsEnrollment ->
            BiometricInteractiveUiState.NeedsEnrollment(canUseDeviceCredential)

        BiometricGateUiState.DeviceCredentialOnly -> BiometricInteractiveUiState.DeviceCredentialOnly
        is BiometricGateUiState.HardwareTemporarilyUnavailable ->
            BiometricInteractiveUiState.HardwareTemporarilyUnavailable(canUseDeviceCredential)

        BiometricGateUiState.SetupSecurityInSettings -> BiometricInteractiveUiState.SetupSecurityInSettings
        is BiometricGateUiState.LegacyBiometricUnsupported ->
            BiometricInteractiveUiState.LegacyBiometricUnsupported(canUseDeviceCredential)
    }
