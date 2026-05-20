package com.example.presentation.auth.biometric.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.presentation.auth.biometric.model.BiometricInteractiveUiState
import com.example.presentation.auth.biometric.platform.authenticateWithDeviceCredential

@Composable
internal fun BiometricAuthInteractiveScreen(
    interactive: BiometricInteractiveUiState,
    promptError: String?,
    onPromptErrorChange: (String?) -> Unit,
    onRefreshGate: () -> Unit,
    onAuthenticated: () -> Unit,
    keyguardLauncher: ActivityResultLauncher<Intent>,
    context: Context
) {
    val description = biometricDescriptionForInteractive(interactive)

    val runDeviceCredential: () -> Unit = {
        onPromptErrorChange(null)
        authenticateWithDeviceCredential(
            context = context,
            keyguardLauncher = keyguardLauncher,
            onSuccessFromSystemUi = onAuthenticated,
            onError = { msg -> onPromptErrorChange(msg.takeUnless { it.isBlank() }) },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (interactive) {
                BiometricInteractiveUiState.BiometricReady -> BiometricDualMethodHero(emphasized = true)
                is BiometricInteractiveUiState.NeedsEnrollment -> BiometricDualMethodHero(emphasized = false)
                else -> BiometricHeroGlyph(biometricHeroIcon(interactive))
            }
            Spacer(modifier = Modifier.height(28.dp))
            BiometricHeadlineBlock(
                title = biometricTitleForInteractive(interactive),
                description = description.takeIf { it.isNotEmpty() },
            )
            BiometricErrorBanner(promptError = promptError)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp,
            shadowElevation = 0.dp,
        ) {
            Column(modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                BiometricAuthActionColumn(
                    interactive = interactive,
                    context = context,
                    onPromptErrorChange = onPromptErrorChange,
                    onRefreshGate = onRefreshGate,
                    onAuthenticated = onAuthenticated,
                    runDeviceCredential = runDeviceCredential,
                )
            }
        }
    }
}
