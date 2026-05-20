package com.example.presentation.auth.biometric.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.presentation.auth.AmbientFlowBackdrop

@Composable
internal fun BiometricAuthScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    AmbientFlowBackdrop(modifier = modifier, content = content)
}
