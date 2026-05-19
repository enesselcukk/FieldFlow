package com.example.presentation.auth.biometric.platform

import android.content.Context
import androidx.biometric.BiometricPrompt
import com.example.presentation.R

@Suppress("MagicNumber")
internal fun userMessageForBiometricError(context: Context, errorCode: Int): String {
    return when (errorCode) {
        BiometricPrompt.ERROR_CANCELED,
        BiometricPrompt.ERROR_USER_CANCELED,
        13,
        -> ""

        BiometricPrompt.ERROR_LOCKOUT,
        BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
        -> context.getString(R.string.biometric_error_lockout)

        BiometricPrompt.ERROR_TIMEOUT ->
            context.getString(R.string.biometric_error_timeout)

        BiometricPrompt.ERROR_NO_BIOMETRICS ->
            context.getString(R.string.biometric_not_enrolled)

        BiometricPrompt.ERROR_HW_UNAVAILABLE,
        BiometricPrompt.ERROR_UNABLE_TO_PROCESS,
        BiometricPrompt.ERROR_NO_SPACE,
        BiometricPrompt.ERROR_HW_NOT_PRESENT,
        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
        BiometricPrompt.ERROR_VENDOR,
        BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED,
        -> context.getString(R.string.biometric_unavailable)

        else -> context.getString(R.string.biometric_unavailable)
    }
}
