package com.lsp.callguard.ui.screen.history

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.lsp.callguard.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun CallHistoryRoute(
    reselection: Flow<Unit> = emptyFlow(),
    onOpenPaywall: () -> Unit
) {
    val viewModel = rememberCallHistoryViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLimitDialog by remember { mutableStateOf(false) }

    val addedMessage = stringResource(R.string.history_added)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryEvent.Added -> {
                    val result = snackbarHostState.showSnackbar(
                        message = addedMessage,
                        actionLabel = undoLabel
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoAdd(event.phoneE164)
                    }
                }

                is HistoryEvent.AddedAfterUpgrade -> snackbarHostState.showSnackbar(addedMessage)

                HistoryEvent.LimitReached -> showLimitDialog = true
            }
        }
    }

    CallHistoryScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        reselection = reselection,
        onClearHistory = viewModel::clearHistory,
        onConfirmAdd = viewModel::addToWhitelist
    )

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text(stringResource(R.string.limit_reached)) },
            text = { Text(stringResource(R.string.history_limit_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLimitDialog = false
                        onOpenPaywall()
                    }
                ) {
                    Text(stringResource(R.string.view_premium))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
