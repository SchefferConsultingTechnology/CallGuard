package com.lsp.callguard.ui.screen.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun CallHistoryRoute(
    onBack: () -> Unit
) {
    val viewModel = rememberCallHistoryViewModel()
    val uiState by viewModel.uiState.collectAsState()

    CallHistoryScreen(
        uiState = uiState,
        onBack = onBack,
        onClearHistory = viewModel::clearHistory
    )
}
