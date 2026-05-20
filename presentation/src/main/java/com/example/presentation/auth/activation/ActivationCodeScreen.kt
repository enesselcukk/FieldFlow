package com.example.presentation.auth.activation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.example.presentation.R
import com.example.presentation.auth.activation.ui.ActivationCodeScaffold

@Composable
fun ActivationCodeScreen(
    expectedCode: String,
    onActivationSuccess: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val invalidCodeText = stringResource(R.string.activation_invalid_code)
    var activationInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun submitActivation() {
        val normalizedInput = activationInput.trim()
        val normalizedExpected = expectedCode.trim()
        if (normalizedInput == normalizedExpected && normalizedInput.length == 6) {
            keyboardController?.hide()
            errorText = null
            onActivationSuccess()
        } else {
            errorText = invalidCodeText
        }
    }

    ActivationCodeScaffold(
        modifier = Modifier.fillMaxSize(),
        activationInput = activationInput,
        onActivationInputChange = {
            activationInput = it.filter(Char::isDigit).take(6)
        },
        errorMessage = errorText,
        onContinue = { submitActivation() },
    )
}
