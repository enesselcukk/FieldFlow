package com.example.presentation.auth.biometric.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.auth.biometric.model.BiometricDualMethodRevealMotion
import kotlinx.coroutines.delay

@Composable
internal fun BiometricDualMethodHero(
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = OrbRowMaxWidthDp.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var entranceReady by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(EntranceDelayMs)
            entranceReady = true
        }

        val fingerprintMotion = rememberBiometricDualMethodRevealMotion(
            entranceReady = entranceReady,
            staggerMillis = 0,
        )
        val faceMotion = rememberBiometricDualMethodRevealMotion(
            entranceReady = entranceReady,
            staggerMillis = FaceStaggerMs,
        )

        BiometricHeroGlowStage(
            emphasized = emphasized,
            fingerprintMotion = fingerprintMotion,
            faceMotion = faceMotion,
        )
    }
}

@Composable
private fun BiometricHeroGlowStage(
    emphasized: Boolean,
    fingerprintMotion: BiometricDualMethodRevealMotion,
    faceMotion: BiometricDualMethodRevealMotion,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometricAmbient")
    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = BreathCycleMs,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GlowStageVerticalPaddingDp.dp),
    ) {
        if (emphasized) {
            BiometricHeroAmbientPulse(breathPhase = breathPhase)
        }
        BiometricDualOrbsRow(
            emphasized = emphasized,
            fingerprintMotion = fingerprintMotion,
            faceMotion = faceMotion,
        )
    }
}

@Composable
private fun BiometricHeroAmbientPulse(breathPhase: Float) {
    val scheme = MaterialTheme.colorScheme
    val haloCore = scheme.primary.copy(alpha = 0.1f + breathPhase * 0.07f)
    val haloEdge = scheme.tertiary.copy(alpha = 0.16f + breathPhase * 0.08f)
    val glowAlpha = haloCore.alpha.coerceAtMost(0.28f).coerceAtLeast(0.08f)
    val glowScale = 0.97f + breathPhase * 0.05f

    Box(
        modifier = Modifier
            .size(
                width = HaloWidthDp.dp,
                height = HaloHeightDp.dp,
            )
            .graphicsLayer {
                alpha = glowAlpha
                scaleX = glowScale
                scaleY = glowScale
            }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        haloCore.copy(alpha = 0f),
                        haloEdge,
                        haloCore.copy(alpha = 0f),
                    ),
                ),
                shape = RoundedCornerShape(percent = 50),
            )
    )
}

@Composable
private fun BiometricDualOrbsRow(
    emphasized: Boolean,
    fingerprintMotion: BiometricDualMethodRevealMotion,
    faceMotion: BiometricDualMethodRevealMotion,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(OrbRowSpacingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BiometricMethodOrb(
            icon = Icons.Outlined.Fingerprint,
            label = stringResource(R.string.biometric_method_fingerprint),
            emphasized = emphasized,
            modifier = Modifier.biometricDualMethodReveal(fingerprintMotion),
        )
        BiometricMethodJoiner()
        BiometricMethodOrb(
            icon = Icons.Outlined.Face,
            label = stringResource(R.string.biometric_method_face),
            emphasized = emphasized,
            modifier = Modifier.biometricDualMethodReveal(faceMotion),
        )
    }
}

@Composable
private fun BiometricMethodJoiner() {
    Text(
        text = stringResource(R.string.biometric_methods_joiner),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.82f),
    )
}

@Composable
private fun BiometricMethodOrb(
    icon: ImageVector,
    label: String,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(OrbLabelIconGapDp.dp),
    ) {
        BiometricOrbGraphic(icon = icon, emphasized = emphasized)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BiometricOrbGraphic(
    icon: ImageVector,
    emphasized: Boolean,
) {
    val ringAlpha = if (emphasized) 0.52f else 0.38f
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(OrbOuterDiameterDp.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = OrbRingWidthDp.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = ringAlpha),
                    shape = CircleShape,
                ),
        )
        OrbInnerDisc(icon = icon, emphasized = emphasized)
    }
}

@Composable
private fun OrbInnerDisc(
    icon: ImageVector,
    emphasized: Boolean,
) {
    Surface(
        modifier = Modifier.size(OrbInnerDiameterDp.dp),
        shape = CircleShape,
        color = if (emphasized) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = if (emphasized) 4.dp else 1.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(OrbIconSizeDp.dp),
                tint = if (emphasized) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
