package com.lsp.callguard.ui.screen.whitelist

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.lsp.callguard.R

@Composable
fun WhitelistRoute(
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    val viewModel = rememberWhitelistViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showLimitDialog by remember { mutableStateOf(false) }
    val numberAlreadyExistsMessage = stringResource(R.string.whitelist_number_already_exists)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                WhitelistEvent.OpenLimitDialog -> {
                    showLimitDialog = true
                }

                WhitelistEvent.NumberAlreadyExists -> {
                    snackbarHostState.showSnackbar(
                        message = numberAlreadyExistsMessage
                    )
                }
            }
        }
    }

    WhitelistScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        showLimitDialog = showLimitDialog,
        onDismissLimitDialog = {
            showLimitDialog = false
        },
        onBack = onBack,
        onOpenPaywall = onOpenPaywall,
        onAddNumber = viewModel::addNumber,
        onDeleteNumber = viewModel::deleteNumber
    )
}