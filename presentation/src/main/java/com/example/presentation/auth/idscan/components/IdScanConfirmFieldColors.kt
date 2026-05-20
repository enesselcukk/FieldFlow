package com.example.presentation.auth.idscan.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

@Composable
internal fun outlinedFieldColorsStrongLabel(): TextFieldColors {
    val scheme = MaterialTheme.colorScheme
    val darkUi = scheme.surface.luminance() < 0.35f
    val insetSurface = if (darkUi) scheme.surfaceContainerHighest else scheme.surfaceContainerLowest
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = insetSurface,
        unfocusedContainerColor = insetSurface,
        disabledContainerColor = insetSurface,
        errorContainerColor = insetSurface,
        cursorColor = scheme.primary,
        errorCursorColor = scheme.error,
        focusedLabelColor = scheme.primary,
        unfocusedLabelColor = scheme.onSurfaceVariant,
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outline,
        disabledBorderColor = scheme.outlineVariant,
        errorBorderColor = scheme.error,
        unfocusedTextColor = scheme.onSurface,
        focusedTextColor = scheme.onSurface,
    )
}
