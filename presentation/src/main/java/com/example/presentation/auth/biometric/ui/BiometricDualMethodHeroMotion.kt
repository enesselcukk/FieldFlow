package com.example.presentation.auth.biometric.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.example.presentation.auth.biometric.model.BiometricDualMethodRevealMotion

internal fun Modifier.biometricDualMethodReveal(motion: BiometricDualMethodRevealMotion): Modifier =
    graphicsLayer {
        alpha = motion.alpha
        translationY = motion.slideYPx
    }
