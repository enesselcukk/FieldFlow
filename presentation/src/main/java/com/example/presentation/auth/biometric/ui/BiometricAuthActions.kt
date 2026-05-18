package com.example.presentation.auth.biometric.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.auth.biometric.model.BiometricInteractiveUiState
import com.example.presentation.auth.biometric.platform.authenticateWithWeakBiometric
import com.example.presentation.auth.biometric.platform.launchBiometricEnrollmentFlow
import com.example.utils.extensions.launchSettingsSafely

@Composable
internal fun BiometricAuthActionColumn(
    interactive: BiometricInteractiveUiState,
    context: Context,
    onPromptErrorChange: (String?) -> Unit,
    onRefreshGate: () -> Unit,
    onAuthenticated: () -> Unit,
    runDeviceCredential: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (interactive) {
            BiometricInteractiveUiState.BiometricReady -> {
                BiometricPrimaryButton(
                    label = stringResource(R.string.biometric_button),
                    onClick = {
                        onPromptErrorChange(null)
                        authenticateWithWeakBiometric(
                            context = context,
                            onSuccess = onAuthenticated,
                            onError = { msg ->
                                onPromptErrorChange(msg.takeUnless { it.isBlank() })
                            }
                        )
                    }
                )
            }

            is BiometricInteractiveUiState.NeedsEnrollment -> {
                BiometricSecondaryButton(
                    label = stringResource(R.string.biometric_open_enrollment_settings),
                    onClick = { context.launchBiometricEnrollmentFlow() },
                )
                if (interactive.canUseDeviceCredential) {
                    BiometricPrimaryButton(
                        label = stringResource(R.string.biometric_use_screen_lock),
                        onClick = runDeviceCredential,
                    )
                } else {
                    BiometricPrimaryButton(
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
                BiometricPrimaryButton(
                    label = stringResource(R.string.biometric_use_screen_lock),
                    onClick = runDeviceCredential,
                )
            }

            is BiometricInteractiveUiState.HardwareTemporarilyUnavailable -> {
                BiometricSecondaryButton(
                    label = stringResource(R.string.biometric_try_again),
                    onClick = onRefreshGate,
                )
                if (interactive.canUseDeviceCredential) {
                    BiometricPrimaryButton(
                        label = stringResource(R.string.biometric_use_screen_lock),
                        onClick = runDeviceCredential,
                    )
                }
            }

            BiometricInteractiveUiState.SetupSecurityInSettings -> {
                BiometricPrimaryButton(
                    label = stringResource(R.string.biometric_open_security_settings),
                    onClick = {
                        context.launchSettingsSafely(
                            Intent(Settings.ACTION_SECURITY_SETTINGS),
                        )
                    }
                )
            }

            is BiometricInteractiveUiState.LegacyBiometricUnsupported -> {
                if (interactive.canUseDeviceCredential) {
                    BiometricPrimaryButton(
                        label = stringResource(R.string.biometric_use_screen_lock),
                        onClick = runDeviceCredential,
                    )
                } else {
                    BiometricPrimaryButton(
                        label = stringResource(R.string.biometric_open_security_settings),
                        onClick = {
                            context.launchSettingsSafely(
                                Intent(Settings.ACTION_SECURITY_SETTINGS),
                            )
                        }
                    )
                }
            }
        }
    }
}
