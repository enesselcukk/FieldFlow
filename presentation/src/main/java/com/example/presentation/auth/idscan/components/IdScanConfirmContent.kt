package com.example.presentation.auth.idscan.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.auth.AmbientFlowBackdrop
import com.example.presentation.auth.idscan.model.IdScanUiState
import kotlinx.coroutines.delay

@Composable
internal fun IdScanConfirmContent(
    modifier: Modifier = Modifier,
    uiState: IdScanUiState,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onScanAgain: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val canContinue = uiState.name.isNotBlank() && uiState.surname.isNotBlank()

    var stageVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(42)
        stageVisible = true
    }

    val fieldShape = RoundedCornerShape(18.dp)
    val formShape = RoundedCornerShape(28.dp)

    AmbientFlowBackdrop(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
            ) {
                IdConfirmEnterSection(
                    modifier = Modifier.fillMaxWidth(),
                    stageVisible = stageVisible,
                    staggerDelayMs = 0,
                ) {
                    Text(
                        text = stringResource(R.string.id_scan_confirm_title),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.id_scan_confirm_description),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(modifier = Modifier.height(26.dp))

                IdConfirmEnterSection(
                    modifier = Modifier.fillMaxWidth(),
                    stageVisible = stageVisible,
                    staggerDelayMs = 100,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = formShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 4.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(22.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 2.dp,
                                shadowElevation = 0.dp,
                            ) {
                                Box(
                                    modifier = Modifier.padding(17.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AssignmentInd,
                                        contentDescription = null,
                                        modifier = Modifier.size(34.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = onNameChange,
                                label = { Text(stringResource(R.string.first_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = fieldShape,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                colors = outlinedFieldColorsStrongLabel(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                ),
                            )
                            OutlinedTextField(
                                value = uiState.surname,
                                onValueChange = onSurnameChange,
                                label = { Text(stringResource(R.string.last_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = fieldShape,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                colors = outlinedFieldColorsStrongLabel(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                        if (canContinue) onContinue()
                                    },
                                ),
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 0.dp,
            ) {
                IdConfirmEnterSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    stageVisible = stageVisible,
                    staggerDelayMs = 220,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        IdScanContinueAnimatedButton(
                            label = stringResource(R.string.continue_button),
                            enabled = canContinue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            onClick = {
                                keyboard?.hide()
                                onContinue()
                            },
                        )

                        IdScanOutlinedActionButton(
                            label = stringResource(R.string.id_scan_scan_again),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            onClick = {
                                keyboard?.hide()
                                onScanAgain()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IdConfirmEnterSection(
    stageVisible: Boolean,
    staggerDelayMs: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val t by animateFloatAsState(
        targetValue = if (stageVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = staggerDelayMs,
            easing = FastOutSlowInEasing,
        ),
        label = "idConfirmReveal",
    )
    Box(
        modifier = modifier.graphicsLayer {
            translationY = (1f - t) * 28f
            alpha = t
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun IdScanContinueAnimatedButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressedScale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.986f else 1f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 1300f,
        ),
        label = "idScanContinuePress",
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.graphicsLayer {
            scaleX = pressedScale
            scaleY = pressedScale
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(26.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor =
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun IdScanOutlinedActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressedScale by animateFloatAsState(
        targetValue = if (pressed) 0.988f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 1500f,
        ),
        label = "idScanOutlinePress",
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = pressedScale
            scaleY = pressedScale
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(26.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
        )
    }
}
