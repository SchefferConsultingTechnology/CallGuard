package com.lsp.callguard.ui.screen.whitelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


@Composable
fun WhitelistRoute(
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val viewModel = rememberWhitelistViewModel()
    val uiState by viewModel.uiState.collectAsState()

    WhitelistScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenPaywall = onOpenPaywall,
        onAddNumber = viewModel::addNumber,
        onDeleteNumber = viewModel::deleteNumber
    )
}

