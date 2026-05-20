package com.example.presentation.auth.biometric.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun BiometricNeedsEnrollmentActionColumn(
    canUseDeviceCredential: Boolean,
    enrollmentLabel: String,
    screenLockLabel: String,
    securitySettingsLabel: String,
    onOpenEnrollmentSettings: () -> Unit,
    onContinueWithScreenLock: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var revealReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(56)
        revealReady = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BiometricRevealStagger(
            revealReady = revealReady,
            staggerDelayMillis = 100,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BiometricEnrollmentLeadingPrimaryButton(
                label = enrollmentLabel,
                onClick = onOpenEnrollmentSettings,
            )
        }
        BiometricRevealStagger(
            revealReady = revealReady,
            staggerDelayMillis = 230,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (canUseDeviceCredential) {
                BiometricEnrollmentTrailingOutlinedButton(
                    label = screenLockLabel,
                    leadingIcon = Icons.Outlined.Lock,
                    onClick = onContinueWithScreenLock,
                )
            } else {
                BiometricEnrollmentTrailingOutlinedButton(
                    label = securitySettingsLabel,
                    leadingIcon = Icons.Outlined.Security,
                    onClick = onOpenSecuritySettings,
                )
            }
        }
    }
}

@Composable
private fun BiometricRevealStagger(
    revealReady: Boolean,
    staggerDelayMillis: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val t by animateFloatAsState(
        targetValue = if (revealReady) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = staggerDelayMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "bioEnrollReveal",
    )
    Box(
        modifier = modifier.graphicsLayer {
            translationY = (1f - t) * 26f
            alpha = t
        },
    ) {
        content()
    }
}

@Composable
private fun BiometricEnrollmentLeadingPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressedScale by animateFloatAsState(
        targetValue = if (pressed) 0.986f else 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 1200f),
        label = "enrollPrimaryPress",
    )

    val breathe = rememberInfiniteTransition(label = "enrollCtaGlow")
    val phase by breathe.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "enrollIconPhase",
    )
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val settingsAlpha = 0.54f + 0.41f * phase

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressedScale
                scaleY = pressedScale
            }
            .heightIn(min = 54.dp),
        shape = RoundedCornerShape(28.dp),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 3.dp,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { alpha = settingsAlpha },
                tint = onPrimary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BiometricEnrollmentTrailingOutlinedButton(
    label: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressedScale by animateFloatAsState(
        targetValue = if (pressed) 0.988f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 1500f),
        label = "enrollOutlinePress",
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressedScale
                scaleY = pressedScale
            }
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(28.dp),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(11.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
            )
        }
    }
}
