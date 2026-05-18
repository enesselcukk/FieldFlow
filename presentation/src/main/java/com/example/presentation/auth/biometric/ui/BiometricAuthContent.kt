package com.example.presentation.auth.biometric.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.auth.biometric.model.BiometricGateUiState
import com.example.presentation.auth.biometric.model.BiometricInteractiveUiState
import com.example.presentation.auth.biometric.model.toInteractiveOrNull
import com.example.presentation.auth.biometric.platform.authenticateWithDeviceCredential
import com.example.presentation.auth.biometric.platform.authenticateWithWeakBiometric
import com.example.presentation.auth.biometric.platform.launchBiometricEnrollmentFlow
import com.example.utils.extensions.launchSettingsSafely

@Composable
internal fun BiometricAuthContent(
    gateState: BiometricGateUiState,
    promptError: String?,
    onPromptErrorChange: (String?) -> Unit,
    onRefreshGate: () -> Unit,
    onAuthenticated: () -> Unit,
    keyguardLauncher: ActivityResultLauncher<Intent>,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (gateState) {
            BiometricGateUiState.Loading -> {
                CircularProgressIndicator()
            }

            BiometricGateUiState.NoHostActivity -> {
                Text(
                    text = stringResource(R.string.biometric_unavailable),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            else -> {
                BiometricAuthInteractiveColumn(
                    interactive = requireNotNull(gateState.toInteractiveOrNull()),
                    promptError = promptError,
                    onPromptErrorChange = onPromptErrorChange,
                    onRefreshGate = onRefreshGate,
                    onAuthenticated = onAuthenticated,
                    keyguardLauncher = keyguardLauncher,
                    context = context,
                )
            }
        }
    }
}

@Composable
private fun BiometricAuthInteractiveColumn(
    interactive: BiometricInteractiveUiState,
    promptError: String?,
    onPromptErrorChange: (String?) -> Unit,
    onRefreshGate: () -> Unit,
    onAuthenticated: () -> Unit,
    keyguardLauncher: ActivityResultLauncher<Intent>,
    context: Context,
) {
    val title = stringResource(R.string.biometric_title)
    val description = biometricDescriptionForInteractive(interactive)

    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (description.isNotEmpty()) {
        Text(text = description)
    }
    if (!promptError.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = promptError,
            color = MaterialTheme.colorScheme.error,
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    val runDeviceCredential: () -> Unit = {
        onPromptErrorChange(null)
        authenticateWithDeviceCredential(
            context = context,
            keyguardLauncher = keyguardLauncher,
            onSuccessFromSystemUi = onAuthenticated,
            onError = { msg -> onPromptErrorChange(msg.takeUnless { it.isBlank() }) },
        )
    }

    when (interactive) {
        BiometricInteractiveUiState.BiometricReady -> {
            BiometricFilledButton(
                label = stringResource(R.string.biometric_button),
                onClick = {
                    onPromptErrorChange(null)
                    authenticateWithWeakBiometric(
                        context = context,
                        onSuccess = onAuthenticated,
                        onError = { msg ->
                            onPromptErrorChange(msg.takeUnless { it.isBlank() })
                        },
                    )
                },
            )
        }

        is BiometricInteractiveUiState.NeedsEnrollment -> {
            OutlinedButton(
                onClick = { context.launchBiometricEnrollmentFlow() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.biometric_open_enrollment_settings))
            }
            if (interactive.canUseDeviceCredential) {
                Spacer(modifier = Modifier.height(12.dp))
                BiometricFilledButton(
                    label = stringResource(R.string.biometric_use_screen_lock),
                    onClick = runDeviceCredential,
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                BiometricFilledButton(
                    label = stringResource(R.string.biometric_open_security_settings),
                    onClick = {
                        context.launchSettingsSafely(
                            Intent(Settings.ACTION_SECURITY_SETTINGS),
                        )
                    },
                )
            }
        }

        BiometricInteractiveUiState.DeviceCredentialOnly -> {
            BiometricFilledButton(
                label = stringResource(R.string.biometric_use_screen_lock),
                onClick = runDeviceCredential,
            )
        }

        is BiometricInteractiveUiState.HardwareTemporarilyUnavailable -> {
            OutlinedButton(
                onClick = onRefreshGate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.biometric_try_again))
            }
            if (interactive.canUseDeviceCredential) {
                Spacer(modifier = Modifier.height(12.dp))
                BiometricFilledButton(
                    label = stringResource(R.string.biometric_use_screen_lock),
                    onClick = runDeviceCredential,
                )
            }
        }

        BiometricInteractiveUiState.SetupSecurityInSettings -> {
            BiometricFilledButton(
                label = stringResource(R.string.biometric_open_security_settings),
                onClick = {
                    context.launchSettingsSafely(
                        Intent(Settings.ACTION_SECURITY_SETTINGS),
                    )
                },
            )
        }

        is BiometricInteractiveUiState.LegacyBiometricUnsupported -> {
            if (interactive.canUseDeviceCredential) {
                BiometricFilledButton(
                    label = stringResource(R.string.biometric_use_screen_lock),
                    onClick = runDeviceCredential,
                )
            } else {
                BiometricFilledButton(
                    label = stringResource(R.string.biometric_open_security_settings),
                    onClick = {
                        context.launchSettingsSafely(
                            Intent(Settings.ACTION_SECURITY_SETTINGS),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun biometricDescriptionForInteractive(state: BiometricInteractiveUiState): String {
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

@Composable
private fun BiometricFilledButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = label)
    }
}
