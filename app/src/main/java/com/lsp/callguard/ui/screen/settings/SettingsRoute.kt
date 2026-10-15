package com.lsp.callguard.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.lsp.callguard.R
import com.lsp.callguard.data.billing.RestoreResult
import com.lsp.callguard.ui.components.tierDisplayName

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit) {
    val viewModel = rememberSettingsViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val restoredMessage = stringResource(R.string.paywall_restore_done, tierDisplayName(uiState.tier))
    val nothingMessage = stringResource(R.string.paywall_restore_none)
    val unavailableMessage = stringResource(R.string.paywall_store_unavailable)

    LaunchedEffect(Unit) {
        viewModel.restoreResults.collect { result ->
            snackbarHostState.showSnackbar(
                when (result) {
                    is RestoreResult.Restored -> restoredMessage
                    RestoreResult.NothingFound -> nothingMessage
                    RestoreResult.StoreUnavailable -> unavailableMessage
                }
            )
        }
    }

    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenPaywall = onOpenPaywall,
        onOpenLanguage = onOpenLanguage,
        onOpenTerms = onOpenTerms,
        onOpenPrivacyPolicy = onOpenPrivacyPolicy,
        onBlockUnknownChange = viewModel::setBlockUnknown,
        onBlockPrivateNumbersChange = viewModel::setBlockPrivateNumbers,
        onUseContactsAutomaticallyChange = viewModel::setUseContactsAutomatically,
        onMockTierChange = viewModel::setMockTier,
        onRestorePurchases = viewModel::restorePurchases,
        snackbarHostState = snackbarHostState
    )
}
