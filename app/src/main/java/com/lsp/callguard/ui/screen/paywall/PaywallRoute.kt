package com.lsp.callguard.ui.screen.paywall

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun PaywallRoute(
    onContinueFreeClick: () -> Unit,
    onPurchased: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = rememberPaywallViewModel()
    val isPremiumActive by viewModel.isPremiumActive.collectAsState()

    LaunchedEffect(isPremiumActive) {
        if (isPremiumActive) {
            onPurchased()
        }
    }

    PaywallScreen(
        onSubscribeClick = {
            (context as? Activity)?.let { viewModel.launchPurchase(it) }
        },
        onContinueFreeClick = onContinueFreeClick
    )
}
