package com.example.presentation.auth.activation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.R
import com.example.presentation.auth.activation.components.ActivationEnterRevealSection
import com.example.presentation.auth.idscan.components.outlinedFieldColorsStrongLabel

private val CardCornerShape = RoundedCornerShape(28.dp)
private val FieldCornerShape = RoundedCornerShape(18.dp)
private val GemIconSize = 32.dp

@Composable
internal fun ActivationCodeFormCard(
    stageVisible: Boolean,
    activationInput: String,
    onActivationInputChange: (String) -> Unit,
    errorMessage: String?,
    onKeyboardDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ActivationEnterRevealSection(
        modifier = modifier.fillMaxWidth(),
        stageVisible = stageVisible,
        staggerDelayMs = 100,
    ) {
        ActivationCodeFilledCardSurface {
            ActivationFormKeyOrb()
            ActivationCodeEntryField(
                value = activationInput,
                onValueChange = onActivationInputChange,
                errorMessage = errorMessage,
                onKeyboardDone = onKeyboardDone,
            )
        }
    }
}

@Composable
private fun ActivationCodeFilledCardSurface(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardCornerShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun ActivationFormKeyOrb() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                modifier = Modifier.size(GemIconSize),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun ActivationCodeEntryField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    onKeyboardDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.activation_code_label)) },
        singleLine = true,
        shape = FieldCornerShape,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Medium,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onKeyboardDone() },
        ),
        isError = errorMessage != null,
        supportingText = {
            AnimatedActivationFieldError(errorMessage.orEmpty())
        },
        colors = outlinedFieldColorsStrongLabel(),
    )
}

@Composable
private fun AnimatedActivationFieldError(errorText: String) {
    AnimatedVisibility(
        visible = errorText.isNotBlank(),
        enter = fadeIn(tween(220)) + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut(tween(180)) + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        Text(
            text = errorText,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
