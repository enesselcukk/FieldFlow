package com.example.presentation.auth.biometric.evaluation

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import com.example.presentation.auth.biometric.model.BiometricGateUiState
import com.example.utils.extensions.findActivity

private const val LogTag = "BiometricAuth"

internal fun evaluateBiometricGate(context: Context): BiometricGateUiState {
    if (context.findActivity() == null) {
        return BiometricGateUiState.NoHostActivity
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        return BiometricGateUiState.LegacyBiometricUnsupported(isKeyguardSecure(context))
    }

    val manager = BiometricManager.from(context)
    val weakStatus = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
    val deviceCredentialReady = isDeviceCredentialReady(context, manager)

    return when (weakStatus) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricGateUiState.BiometricReady
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            BiometricGateUiState.NeedsEnrollment(deviceCredentialReady)

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            if (deviceCredentialReady) {
                BiometricGateUiState.DeviceCredentialOnly
            } else {
                BiometricGateUiState.SetupSecurityInSettings
            }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            BiometricGateUiState.HardwareTemporarilyUnavailable(deviceCredentialReady)

        else -> {
            Log.w(LogTag, "BiometricManager.canAuthenticate(weak) status=$weakStatus")
            if (deviceCredentialReady) {
                BiometricGateUiState.HardwareTemporarilyUnavailable(true)
            } else {
                BiometricGateUiState.SetupSecurityInSettings
            }
        }
    }
}

private fun isKeyguardSecure(context: Context): Boolean {
    val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguard.isKeyguardSecure
}

private fun isDeviceCredentialReady(context: Context, manager: BiometricManager): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        manager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS
    } else {
        isKeyguardSecure(context)
    }
}
