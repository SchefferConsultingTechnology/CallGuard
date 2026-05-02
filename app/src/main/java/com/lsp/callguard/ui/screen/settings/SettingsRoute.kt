package com.lsp.callguard.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    val viewModel = rememberSettingsViewModel()
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenPaywall = onOpenPaywall,
        onOpenLanguage = onOpenLanguage,
        onOpenTerms = onOpenTerms,
        onOpenPrivacyPolicy = onOpenPrivacyPolicy,
        onBlockUnknownChange = viewModel::setBlockUnknown,
        onBlockPrivateNumbersChange = viewModel::setBlockPrivateNumbers,
        onUseContactsAutomaticallyChange = viewModel::setUseContactsAutomatically
    )
}
