package com.example.presentation.auth.biometric.platform

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import com.example.utils.extensions.launchSettingsSafely

internal fun Context.launchBiometricEnrollmentFlow() {
    val primary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_WEAK,
            )
        }
    } else {
        Intent(Settings.ACTION_SECURITY_SETTINGS)
    }
    launchSettingsSafely(primary, Intent(Settings.ACTION_SECURITY_SETTINGS))
}
