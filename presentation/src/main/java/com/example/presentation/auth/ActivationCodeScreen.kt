package com.example.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.activation_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.activation_description))
        Spacer(modifier = Modifier.height(16.dp))

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
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submitActivation() }
            ),
            isError = errorText != null,
        )

        if (errorText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorText.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { submitActivation() },
            modifier = Modifier.fillMaxWidth(),
            enabled = input.length == 6
        ) {
            Text(stringResource(R.string.activation_button))
        }
    }
}
