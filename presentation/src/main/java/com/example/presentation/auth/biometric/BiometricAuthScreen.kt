package com.example.presentation.auth.biometric

import android.Manifest
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.presentation.R
import com.example.utils.extensions.findActivity

@RequiresPermission(Manifest.permission.USE_BIOMETRIC)
@Composable
fun BiometricAuthScreen(
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.biometric_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.biometric_description))

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                errorMessage = null
                authenticateWithBiometric(
                    context = context,
                    onSuccess = onAuthenticated,
                    onError = { errorMessage = it }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.biometric_button))
        }
    }
}

@RequiresPermission(Manifest.permission.USE_BIOMETRIC)
private fun authenticateWithBiometric(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        onError(context.getString(R.string.biometric_unavailable))
        return
    }

    val activity = context.findActivity()
        ?: run {
            onError(context.getString(R.string.biometric_unavailable))
            return
        }

    val manager = BiometricManager.from(activity)
    when (val status = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt.Builder(activity)
                .setTitle(context.getString(R.string.biometric_prompt_title))
                .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
                .setNegativeButton(
                    context.getString(R.string.biometric_prompt_cancel),
                    executor
                ) { _, _ ->
                    onError(context.getString(R.string.biometric_unavailable))
                }
                .build()

            prompt.authenticate(
                CancellationSignal(),
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        onError(errString?.toString().orEmpty())
                    }

                    override fun onAuthenticationFailed() {
                        onError(context.getString(R.string.biometric_failed))
                    }
                }
            )
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> onError(context.getString(R.string.biometric_no_hardware))
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> onError(context.getString(R.string.biometric_unavailable))
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> onError(context.getString(R.string.biometric_not_enrolled))
        else -> onError("${context.getString(R.string.biometric_unavailable)} (code=$status)")
    }
}
