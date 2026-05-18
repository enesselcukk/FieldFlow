package com.example.presentation.auth.biometric

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.presentation.auth.biometric.evaluation.evaluateBiometricGate
import com.example.presentation.auth.biometric.model.BiometricGateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class BiometricViewModel @Inject constructor() : ViewModel() {

    private val _gateState = MutableStateFlow<BiometricGateUiState>(BiometricGateUiState.Loading)
    val gateState: StateFlow<BiometricGateUiState> = _gateState.asStateFlow()

    private val _promptError = MutableStateFlow<String?>(null)
    val promptError: StateFlow<String?> = _promptError.asStateFlow()

    fun refreshGate(hostContext: Context) {
        _gateState.value = evaluateBiometricGate(hostContext)
    }

    fun setPromptError(message: String?) {
        _promptError.value = message
    }
}
