package com.example.presentation.auth.biometric

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.presentation.auth.biometric.ui.BiometricAuthContent

@Composable
fun BiometricAuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: BiometricViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshNonce by remember { mutableIntStateOf(0) }
    val gateState by viewModel.gateState.collectAsStateWithLifecycle()
    val promptError by viewModel.promptError.collectAsStateWithLifecycle()

    val keyguardLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onAuthenticated()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshNonce++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(context, refreshNonce) {
        viewModel.refreshGate(context)
    }

    BiometricAuthContent(
        gateState = gateState,
        promptError = promptError,
        onPromptErrorChange = viewModel::setPromptError,
        onRefreshGate = { refreshNonce++ },
        onAuthenticated = onAuthenticated,
        keyguardLauncher = keyguardLauncher,
    )
}
