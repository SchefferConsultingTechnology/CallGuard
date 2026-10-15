package com.lsp.callguard.ui.screen.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.domain.model.CallDecisionLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun CallHistoryScreen(
    uiState: CallHistoryUiState,
    snackbarHostState: SnackbarHostState,
    reselection: Flow<Unit> = emptyFlow(),
    onClearHistory: () -> Unit,
    onConfirmAdd: (phoneE164: String, label: String) -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var confirmAdd by remember { mutableStateOf<Pair<String, String?>?>(null) }

    val listState = rememberLazyListState()
    LaunchedEffect(reselection) {
        reselection.collect { listState.animateScrollToItem(0) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (uiState.logs.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(stringResource(R.string.clear_history))
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                CallHistoryHeader()
            }

            if (uiState.logs.isEmpty()) {
                item {
                    EmptyHistoryCard()
                }
            } else {
                item {
                    HistorySummaryCard(logs = uiState.logs)
                }

                items(uiState.logs, key = { it.id }) { log ->
                    val displayPhone = uiState.displayPhones[log.id]
                    CallHistoryItemCard(
                        log = log,
                        displayPhone = displayPhone,
                        action = uiState.actions[log.id] ?: QuickAllowAction.NONE,
                        onAdd = {
                            uiState.canonicalPhones[log.id]?.let { phone -> confirmAdd = phone to displayPhone }
                        }
                    )
                }
            }
        }
    }

    confirmAdd?.let { (phone, display) ->
        AddToWhitelistDialog(
            displayPhone = display ?: phone,
            onDismiss = { confirmAdd = null },
            onConfirm = { label ->
                onConfirmAdd(phone, label)
                confirmAdd = null
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_history_dialog_title)) },
            text = { Text(stringResource(R.string.clear_history_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AddToWhitelistDialog(
    displayPhone: String,
    onDismiss: () -> Unit,
    onConfirm: (label: String) -> Unit
) {
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_add_dialog_title)) },
        text = {
            Column {
                Text(
                    text = displayPhone,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.history_add_name_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(label) }) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CallHistoryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.history_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(58.dp)
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.no_history_yet),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.no_history_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistorySummaryCard(
    logs: List<CallDecisionLog>
) {
    val allowed = logs.count { it.allowed }
    val blocked = logs.count { !it.allowed }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.recent_summary),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryPill(
                    icon = Icons.Outlined.Verified,
                    title = stringResource(R.string.allowed_count, allowed),
                    container = CallGuardTheme.extendedColors.successContainer,
                    content = CallGuardTheme.extendedColors.onSuccessContainer
                )

                SummaryPill(
                    icon = Icons.Outlined.Block,
                    title = stringResource(R.string.blocked_count, blocked),
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun SummaryPill(
    icon: ImageVector,
    title: String,
    container: Color,
    content: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun CallHistoryItemCard(
    log: CallDecisionLog,
    displayPhone: String?,
    action: QuickAllowAction,
    onAdd: () -> Unit
) {
    val title = displayPhone?.takeIf { it.isNotBlank() } ?: stringResource(R.string.private_number)
    val statusText = if (log.allowed) stringResource(R.string.status_allowed) else stringResource(R.string.status_blocked)
    val icon = if (log.allowed) Icons.Outlined.Verified else Icons.Outlined.Block
    // Colour is never the only signal: icon and status text differ too (spec FR-008).
    val statusColor = if (log.allowed) CallGuardTheme.extendedColors.success else MaterialTheme.colorScheme.error
    val statusContainer = if (log.allowed) {
        CallGuardTheme.extendedColors.successContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = statusContainer,
                modifier = Modifier.size(42.dp)
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                val reason = formatReason(log.reason)
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = statusColor, fontWeight = FontWeight.Medium)) {
                            append(statusText)
                        }
                        append(" • ")
                        append(reason)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDate(log.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (action) {
                    QuickAllowAction.ADD -> TextButton(
                        onClick = onAdd,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.history_add_to_whitelist))
                    }

                    QuickAllowAction.IN_WHITELIST -> {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.history_in_whitelist),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    QuickAllowAction.NONE -> Unit
                }
            }
        }
    }
}

@Composable
private fun formatReason(reason: String): String {
    return when (reason) {
        "ALLOWED_BY_WHITELIST" -> stringResource(R.string.reason_whitelist)
        "ALLOWED_UNKNOWN_DISABLED" -> stringResource(R.string.reason_unknown_disabled)
        "BLOCKED_NOT_IN_WHITELIST" -> stringResource(R.string.reason_blocked_not_in_whitelist)
        "BLOCKED_PRIVATE_NUMBER" -> stringResource(R.string.reason_blocked_private_number)
        "ALLOWED_PRIVATE_NUMBER_DISABLED" -> stringResource(R.string.reason_private_disabled)
        "ALLOWED_BY_CONTACTS" -> stringResource(R.string.reason_allowed_by_contacts)
        "BLOCKED_UNKNOWN_UNPARSEABLE" -> stringResource(R.string.reason_unparseable_blocked)
        "ALLOWED_UNPARSEABLE_UNKNOWN_DISABLED" -> stringResource(R.string.reason_unparseable_allowed)
        "ALLOWED_NORMALIZATION_ERROR" -> stringResource(R.string.reason_normalization_error)
        // legacy rows written before 001
        "INVALID_NUMBER" -> stringResource(R.string.reason_invalid_number)
        else -> reason
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

