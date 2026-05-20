package com.example.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun AmbientFlowBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val wash = remember(scheme.surfaceContainerLow, scheme.surface, scheme.surfaceContainerLowest) {
        Brush.verticalGradient(
            colors = listOf(
                scheme.surfaceContainerLow,
                scheme.surface,
                scheme.surfaceContainerLowest,
            ),
        )
    }
    val upperBlob = remember(scheme.primary, scheme.tertiary) {
        Brush.radialGradient(
            colors = listOf(
                scheme.primary.copy(alpha = 0.22f),
                scheme.tertiary.copy(alpha = 0.06f),
                Color.Transparent,
            ),
        )
    }
    val lowerBlob = remember(scheme.tertiary, scheme.secondary) {
        Brush.radialGradient(
            colors = listOf(
                scheme.tertiary.copy(alpha = 0.18f),
                scheme.secondary.copy(alpha = 0.04f),
                Color.Transparent,
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(wash),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = (-72).dp)
                .size(288.dp)
                .clip(CircleShape)
                .background(brush = upperBlob),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-104).dp, y = 48.dp)
                .size(320.dp)
                .clip(CircleShape)
                .background(brush = lowerBlob),
        )
        content()
    }
}
