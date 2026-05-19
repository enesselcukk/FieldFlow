package com.example.presentation.auth.biometric.platform

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.presentation.R
import com.example.utils.extensions.findActivity

private const val LogTag = "BiometricAuth"

internal fun authenticateWithDeviceCredential(
    context: Context,
    keyguardLauncher: ActivityResultLauncher<Intent>,
    onSuccessFromSystemUi: () -> Unit,
    onError: (String) -> Unit,
) {
    val activity = context.findActivity() ?: run {
        onError(context.getString(R.string.biometric_unavailable))
        return
    }
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            startDeviceCredentialPrompt(
                activity,
                context,
                onSuccessFromSystemUi,
                onError,
            )
        }
        else -> {
            launchKeyguardConfirmIntent(
                context,
                keyguardLauncher,
                onError,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private fun startDeviceCredentialPrompt(
    activity: Activity,
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val fragmentActivity = activity as? FragmentActivity ?: run {
        onError(context.getString(R.string.biometric_unavailable))
        return
    }
    val executor = ContextCompat.getMainExecutor(context)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Log.w(LogTag, "Device credential error code=$errorCode detail=$errString")
            onError(userMessageForBiometricError(context, errorCode))
        }
        override fun onAuthenticationFailed() {
            onError(context.getString(R.string.biometric_failed))
        }
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.biometric_prompt_title))
        .setSubtitle(context.getString(R.string.biometric_prompt_device_credential_subtitle))
        .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    BiometricPrompt(fragmentActivity, executor, callback).authenticate(promptInfo)
}

@Suppress("DEPRECATION")
private fun launchKeyguardConfirmIntent(
    context: Context,
    keyguardLauncher: ActivityResultLauncher<Intent>,
    onError: (String) -> Unit,
) {
    val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    if (!keyguard.isKeyguardSecure) {
        onError(context.getString(R.string.biometric_unavailable))
        return
    }
    val intent = keyguard.createConfirmDeviceCredentialIntent(
        context.getString(R.string.biometric_prompt_title),
        context.getString(R.string.biometric_prompt_device_credential_subtitle),
    )
    if (intent == null) {
        onError(context.getString(R.string.biometric_unavailable))
        return
    }
    keyguardLauncher.launch(intent)
}
