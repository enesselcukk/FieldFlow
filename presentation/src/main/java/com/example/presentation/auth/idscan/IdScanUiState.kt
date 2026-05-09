package com.example.presentation.auth.idscan

data class IdScanUiState(
    val isLoading: Boolean = false,
    val errorText: String? = null,
    val name: String = "",
    val surname: String = ""
)
