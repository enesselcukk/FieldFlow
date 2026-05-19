package com.example.presentation.auth.idscan.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

@Composable
internal fun outlinedFieldColorsStrongLabel(): TextFieldColors {
    val scheme = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedLabelColor = scheme.primary,
        unfocusedLabelColor = scheme.onSurfaceVariant,
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outline,
        unfocusedTextColor = scheme.onSurface,
        focusedTextColor = scheme.onSurface,
    )
}
