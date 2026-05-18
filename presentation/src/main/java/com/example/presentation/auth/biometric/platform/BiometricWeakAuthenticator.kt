package com.example.presentation.auth.biometric.platform

import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.presentation.R
import com.example.utils.extensions.findActivity

private const val LogTag = "BiometricAuth"

internal fun authenticateWithWeakBiometric(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        onError(context.getString(R.string.biometric_unavailable))
        return
    }

    val activity = context.findActivity() ?: run {
        onError(context.getString(R.string.biometric_unavailable))
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            Log.w(LogTag, "Weak biometric error code=$errorCode detail=$errString")
            onError(userMessageForPlatformBiometricError(context, errorCode))
        }

        override fun onAuthenticationFailed() {
            onError(context.getString(R.string.biometric_failed))
        }
    }

    val builder = BiometricPrompt.Builder(activity)
        .setTitle(context.getString(R.string.biometric_prompt_title))
        .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
        .setNegativeButton(
            context.getString(R.string.biometric_prompt_cancel),
            executor,
        ) { _, _ -> onError("") }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
    }

    val prompt = builder.build()
    prompt.authenticate(CancellationSignal(), executor, callback)
}
