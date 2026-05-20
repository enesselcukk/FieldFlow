package com.example.presentation.auth.activation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.auth.activation.components.ActivationEnterRevealSection
import com.example.presentation.auth.activation.components.ActivationPrimaryButton

@Composable
internal fun ActivationCodeBottomBar(
    stageVisible: Boolean,
    canActivate: Boolean,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 0.dp,
    ) {
        ActivationEnterRevealSection(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            stageVisible = stageVisible,
            staggerDelayMs = 220,
        ) {
            ActivationPrimaryButton(
                label = stringResource(R.string.activation_button),
                enabled = canActivate,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                onClick = onActivate,
            )
        }
    }
}
