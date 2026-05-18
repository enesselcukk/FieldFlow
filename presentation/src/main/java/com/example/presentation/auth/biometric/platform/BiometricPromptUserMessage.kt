package com.example.presentation.auth.biometric.platform

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import com.example.presentation.R

@Suppress("MagicNumber")
internal fun userMessageForPlatformBiometricError(context: Context, errorCode: Int): String {
    return when (errorCode) {
        BiometricPrompt.BIOMETRIC_ERROR_CANCELED,
        BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED,
        13,
        -> ""

        BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT,
        BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT,
        -> context.getString(R.string.biometric_error_lockout)

        BiometricPrompt.BIOMETRIC_ERROR_TIMEOUT ->
            context.getString(R.string.biometric_error_timeout)

        BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS ->
            context.getString(R.string.biometric_not_enrolled)

        BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BiometricPrompt.BIOMETRIC_ERROR_UNABLE_TO_PROCESS,
        BiometricPrompt.BIOMETRIC_ERROR_NO_SPACE,
        BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT,
        BiometricPrompt.BIOMETRIC_ERROR_NO_DEVICE_CREDENTIAL,
        BiometricPrompt.BIOMETRIC_ERROR_VENDOR,
        BiometricPrompt.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
        -> context.getString(R.string.biometric_unavailable)

        else -> context.getString(R.string.biometric_unavailable)
    }
}
