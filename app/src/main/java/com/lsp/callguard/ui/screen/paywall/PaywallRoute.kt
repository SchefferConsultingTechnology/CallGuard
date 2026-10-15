package com.lsp.callguard.ui.screen.paywall

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.lsp.callguard.R
import com.lsp.callguard.data.billing.RestoreResult
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.ui.components.tierDisplayName

@Composable
fun PaywallRoute(
    onContinueFreeClick: () -> Unit,
    onPurchased: () -> Unit
) {
    val activity = LocalActivity.current
    val viewModel = rememberPaywallViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val restoredMessage = stringResource(R.string.paywall_restore_done, tierDisplayName(uiState.currentTier))
    val nothingMessage = stringResource(R.string.paywall_restore_none)
    val unavailableMessage = stringResource(R.string.paywall_store_unavailable)

    // Leaves the offer when the plan goes up while it is open (a purchase completed).
    val tierWhenOpened = remember { mutableTierHolder() }
    LaunchedEffect(uiState.currentTier) {
        val opened = tierWhenOpened.value
        if (opened == null) {
            tierWhenOpened.value = uiState.currentTier
        } else if (uiState.currentTier > opened) {
            onPurchased()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is PaywallEvent.Restore -> when (event.result) {
                    is RestoreResult.Restored -> restoredMessage
                    RestoreResult.NothingFound -> nothingMessage
                    RestoreResult.StoreUnavailable -> unavailableMessage
                }
                PaywallEvent.PurchaseUnavailable -> unavailableMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    PaywallScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSelectPeriod = viewModel::selectPeriod,
        onBuy = { offer -> activity?.let { viewModel.buy(it, offer) } },
        onRetry = viewModel::retry,
        onRestore = viewModel::restore,
        onContinueFreeClick = onContinueFreeClick
    )
}

private class TierHolder(var value: Tier? = null)

private fun mutableTierHolder() = TierHolder()
