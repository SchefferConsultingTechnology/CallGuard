package com.lsp.callguard.ui.screen.whitelist

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Upgrade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R
import com.lsp.callguard.core.theme.CallGuardTheme

@Composable
fun WhitelistScreen(
    uiState: WhitelistUiState,
    snackbarHostState: SnackbarHostState,
    showLimitDialog: Boolean,
    onDismissLimitDialog: () -> Unit,
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    phoneExample: String?,
    previewFor: (String) -> PhoneInputPreview,
    onAddNumber: (label: String, rawInput: String) -> Unit,
    onDeleteNumber: (id: String) -> Unit
){


    var showAddDialog by remember { mutableStateOf(false) }

    val count = uiState.count
    val limit = uiState.limit ?: 0
    val progress = uiState.progress.coerceIn(0f, 1f)
    val hasPaidPlan = uiState.hasPaidPlan
    val reachedLimit = uiState.reachedLimit
    val numbers = uiState.numbers

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                           showAddDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(stringResource(R.string.whitelist_add_number))
                }
            }
        }
    ) { innerPadding: PaddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp)
        ) {
            item {
                WhitelistHeader(
                    onBack = onBack
                )
            }

            item {
                WhitelistUsageCard(
                    count = count,
                    limit = limit,
                    progress = progress,
                    hasPaidPlan = hasPaidPlan,
                    onOpenPaywall = onOpenPaywall
                )
            }

            if (numbers.isEmpty()) {
                item {
                    EmptyWhitelistCard()
                }
            } else {
                items(numbers, key = { it.id }) { item ->
                    WhitelistNumberCard(
                        item = item,
                        onDelete = {
                            onDeleteNumber(item.id)
                        }
                    )
                }
            }

            if (!hasPaidPlan) {
                item {
                    PremiumHintCard(
                        onOpenPaywall = onOpenPaywall
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWhitelistNumberDialog(
            phoneExample = phoneExample,
            previewFor = previewFor,
            onDismiss = { showAddDialog = false },
            onConfirm = { label, phone ->
                onAddNumber(label, phone)
                showAddDialog = false
            }
        )
    }

    if (showLimitDialog && !hasPaidPlan) {
        AlertDialog(
            onDismissRequest = onDismissLimitDialog,
            title = {
                Text(stringResource(R.string.limit_reached))
            },
            text = {
                Text(stringResource(R.string.limit_reached_text, uiState.limit ?: 0))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissLimitDialog()
                        onOpenPaywall()
                    }
                ) {
                    Text(stringResource(R.string.view_premium))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissLimitDialog
                ) {
                    Text(stringResource(R.string.now_no))
                }
            }
        )
    }
}

@Composable
private fun WhitelistHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.whitelist_header_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.whitelist_header_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WhitelistUsageCard(
    count: Int,
    limit: Int,
    progress: Float,
    hasPaidPlan: Boolean,
    onOpenPaywall: () -> Unit
) {
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
            WhitelistCardHeader(
                icon = if (hasPaidPlan) Icons.Outlined.Star else Icons.Outlined.Lock,
                title = if (hasPaidPlan) stringResource(R.string.whitelist_unlimited) else stringResource(R.string.whitelist_free_version),
                description = if (hasPaidPlan) {
                    stringResource(R.string.whitelist_premium_description)
                } else {
                    stringResource(R.string.whitelist_usage, count, limit)
                }
            )

            if (!hasPaidPlan) {
                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (count >= limit) {
                        CallGuardTheme.extendedColors.warning
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.whitelist_premium_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onOpenPaywall,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Upgrade,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(stringResource(R.string.learn_premium))
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.add_numbers_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyWhitelistCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(54.dp)
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.no_numbers_yet),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.add_numbers_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WhitelistNumberCard(
    item: WhitelistItemUi,
    onDelete: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.displayPhone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumHintCard(
    onOpenPaywall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            WhitelistCardHeader(
                icon = Icons.Outlined.Star,
                title = stringResource(R.string.premium_automate_title),
                description = stringResource(R.string.premium_automate_description)
            )

            Spacer(modifier = Modifier.height(14.dp))

            FilledTonalButton(
                onClick = onOpenPaywall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.view_premium_benefits))
            }
        }
    }
}

@Composable
private fun AddWhitelistNumberDialog(
    phoneExample: String?,
    previewFor: (String) -> PhoneInputPreview,
    onDismiss: () -> Unit,
    onConfirm: (label: String, phone: String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val preview = previewFor(phone)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.add_number_dialog_title))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.label_name_or_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.phone_input_label)) },
                    placeholder = phoneExample?.let { example -> { Text(example) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = preview is PhoneInputPreview.Invalid,
                    supportingText = {
                        when (preview) {
                            is PhoneInputPreview.Valid -> {
                                Text(stringResource(R.string.phone_preview, preview.display))
                            }

                            PhoneInputPreview.Invalid -> {
                                Text(stringResource(R.string.phone_invalid_help))
                            }

                            PhoneInputPreview.Empty -> Unit
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(label, phone) },
                enabled = preview is PhoneInputPreview.Valid
            ) {
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
private fun WhitelistCardHeader(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(38.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp)
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

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
