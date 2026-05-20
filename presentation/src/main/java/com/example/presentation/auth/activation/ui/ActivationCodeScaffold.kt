package com.example.presentation.auth.activation.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.presentation.auth.AmbientFlowBackdrop
import kotlinx.coroutines.delay

private val ActivationMainHorizontalPadding = 24.dp
private val ActivationMainTopPadding = 8.dp
private val ActivationMainBottomPadding = 24.dp
private val HeadlineFormGap = 26.dp

@Composable
internal fun ActivationCodeScaffold(
    activationInput: String,
    onActivationInputChange: (String) -> Unit,
    errorMessage: String?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var stageVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(42)
        stageVisible = true
    }

    AmbientFlowBackdrop(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ActivationCodeMainScrollPane(scrollState = scrollState) {
                ActivationCodeHeadlineBlock(stageVisible = stageVisible)
                Spacer(modifier = Modifier.height(HeadlineFormGap))
                ActivationCodeFormCard(
                    stageVisible = stageVisible,
                    activationInput = activationInput,
                    onActivationInputChange = onActivationInputChange,
                    errorMessage = errorMessage,
                    onKeyboardDone = onContinue,
                )
            }
            ActivationCodeBottomBar(
                stageVisible = stageVisible,
                canActivate = activationInput.length == 6,
                onActivate = onContinue,
            )
        }
    }
}

@Composable
private fun ColumnScope.ActivationCodeMainScrollPane(
    scrollState: ScrollState,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = ActivationMainHorizontalPadding)
            .padding(top = ActivationMainTopPadding, bottom = ActivationMainBottomPadding),
    ) {
        content()
    }
}
