package com.example.presentation.auth.biometric.ui

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.presentation.auth.biometric.model.BiometricGateUiState
import com.example.presentation.auth.biometric.model.toInteractiveOrNull

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

    BiometricAuthScreenBackground {
        when (gateState) {
            BiometricGateUiState.Loading -> BiometricLoadingContent()

            BiometricGateUiState.NoHostActivity -> BiometricUnavailableContent()

            else -> {
                BiometricAuthInteractiveScreen(
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
