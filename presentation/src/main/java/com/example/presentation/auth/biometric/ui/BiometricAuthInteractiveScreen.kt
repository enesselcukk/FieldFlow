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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
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
            BiometricHeroGlyph(biometricHeroIcon(interactive))
            Spacer(modifier = Modifier.height(32.dp))
            BiometricHeadlineBlock(
                title = stringResource(R.string.biometric_title),
                description = description.takeIf { it.isNotEmpty() },
            )
            BiometricErrorBanner(promptError = promptError)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
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
