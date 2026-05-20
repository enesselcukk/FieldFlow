package com.example.presentation.auth.activation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
internal fun ActivationEnterRevealSection(
    stageVisible: Boolean,
    staggerDelayMs: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val t by animateFloatAsState(
        targetValue = if (stageVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 520,
            delayMillis = staggerDelayMs,
            easing = FastOutSlowInEasing,
        ),
        label = "activationReveal",
    )
    Box(
        modifier = modifier.graphicsLayer {
            translationY = (1f - t) * 26f
            alpha = t
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
