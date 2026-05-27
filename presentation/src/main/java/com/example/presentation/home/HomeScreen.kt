package com.example.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.home.runtime.HomeRuntimePermissionHost

@Composable
fun HomeScreen(
    onNavigateToMap: () -> Unit = {},
    onNavigateToEventLog: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeRuntimePermissionHost(
        uiState = uiState,
        viewModel = viewModel,
        onNavigateToMap = onNavigateToMap,
        onNavigateToEventLog = onNavigateToEventLog,
    )
}
