package com.example.presentation.auth.activation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.auth.activation.components.ActivationEnterRevealSection

@Composable
internal fun ActivationCodeHeadlineBlock(
    stageVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    ActivationEnterRevealSection(
        modifier = modifier.fillMaxWidth(),
        stageVisible = stageVisible,
        staggerDelayMs = 0,
    ) {
        Text(
            text = stringResource(R.string.activation_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.activation_description),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
            textAlign = TextAlign.Start,
        )
    }
}
