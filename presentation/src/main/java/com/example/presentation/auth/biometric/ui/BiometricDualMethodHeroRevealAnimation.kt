package com.example.presentation.auth.biometric.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.presentation.auth.biometric.model.BiometricDualMethodRevealMotion

@Composable
internal fun rememberBiometricDualMethodRevealMotion(
    entranceReady: Boolean,
    staggerMillis: Int,
): BiometricDualMethodRevealMotion {
    val alpha by animateFloatAsState(
        targetValue = if (entranceReady) 1f else 0f,
        animationSpec = tween(
            durationMillis = EntranceTweenMs,
            delayMillis = staggerMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "bioHeroAlpha-$staggerMillis",
    )
    val slidePx by animateFloatAsState(
        targetValue = if (entranceReady) 0f else EntranceSlideOffsetPx,
        animationSpec = tween(
            durationMillis = EntranceTweenMs,
            delayMillis = staggerMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "bioHeroSlide-$staggerMillis",
    )
    return BiometricDualMethodRevealMotion(alpha = alpha, slideYPx = slidePx)
}
