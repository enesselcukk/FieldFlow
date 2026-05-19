package com.example.presentation.auth.activation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.presentation.R

@Composable
fun ActivationCodeScreen(
    expectedCode: String,
    onActivationSuccess: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val invalidCodeText = stringResource(R.string.activation_invalid_code)
    var input by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    fun submitActivation() {
        val normalizedInput = input.trim()
        val normalizedExpected = expectedCode.trim()
        if (normalizedInput == normalizedExpected && normalizedInput.length == 6) {
            keyboardController?.hide()
            onActivationSuccess()
        } else {
            errorText = invalidCodeText
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.activation_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(text = stringResource(R.string.activation_description))
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it.filter(Char::isDigit).take(6)
                    errorText = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.activation_code_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submitActivation() },
                ),
                isError = errorText != null,
            )

            if (errorText != null) {
                Text(
                    text = errorText.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
        ) {
            Button(
                onClick = { submitActivation() },
                modifier = Modifier.fillMaxWidth(),
                enabled = input.length == 6,
            ) {
                Text(stringResource(R.string.activation_button))
            }
        }
    }
}
