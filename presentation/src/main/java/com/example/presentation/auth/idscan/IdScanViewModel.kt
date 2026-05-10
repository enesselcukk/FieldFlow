package com.example.presentation.auth.idscan

import androidx.lifecycle.ViewModel
import com.example.domain.model.IdentityInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class IdScanViewModel @Inject constructor(
    private val identityTextParser: IdentityTextParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdScanUiState())
    val uiState: StateFlow<IdScanUiState> = _uiState.asStateFlow()

    fun onOcrSuccess(rawText: String, notFoundMessage: String) {
        val parsed = identityTextParser.parse(rawText)
        if (parsed.name.isBlank() && parsed.surname.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorText = notFoundMessage) }
        } else {
            _uiState.update {
                it.copy(isLoading = false, errorText = null, name = parsed.name, surname = parsed.surname)
            }
        }
    }

    fun onOcrError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorText = message) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading, errorText = if (loading) null else it.errorText) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onSurnameChanged(value: String) {
        _uiState.update { it.copy(surname = value) }
    }

    fun buildIdentityInfo(): IdentityInfo {
        val state = _uiState.value
        return IdentityInfo(name = state.name.trim(), surname = state.surname.trim())
    }

    fun clearDetectedIdentity() {
        _uiState.update { it.copy(name = "", surname = "", errorText = null) }
    }
}
