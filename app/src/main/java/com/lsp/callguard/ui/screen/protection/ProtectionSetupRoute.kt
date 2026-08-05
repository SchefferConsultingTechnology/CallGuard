package com.lsp.callguard.ui.screen.protection

import androidx.compose.runtime.Composable

@Composable
fun ProtectionSetupRoute(
    onBack: () -> Unit
) {
    val viewModel = rememberProtectionSetupViewModel()

    ProtectionSetupScreen(
        onBack = onBack,
        onProtectionStateChanged = viewModel::syncProtectionState
    )
}
