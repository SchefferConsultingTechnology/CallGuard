package com.lsp.callguard.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneLocked
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.domain.billing.EntitlementStatus
import com.lsp.callguard.domain.model.PlanFeatures
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.ui.components.tierDisplayName

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenWhitelist: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenProtectionSetup: () -> Unit,
    onOpenCallHistory: () -> Unit,
    onOpenContactsConsent: () -> Unit,
    onDismissDowngradeNotice: () -> Unit
) {
    val hasPaidPlan = uiState.hasPaidPlan
    val whitelistCount = uiState.whitelistCount
    val limit = uiState.whitelistLimit
    val progress = uiState.progress

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp)
        ) {
            item {
                HomeHeader(
                    onOpenSettings = onOpenSettings
                )
            }

            uiState.downgradedFrom?.let { previousTier ->
                item {
                    DowngradeNoticeCard(
                        previousTier = previousTier,
                        currentTier = uiState.tier,
                        onOpenPaywall = onOpenPaywall,
                        onDismiss = onDismissDowngradeNotice
                    )
                }
            }

            item {
                ProtectionStatusCard(
                    isProtectionEnabled = uiState.isProtectionEnabled,
                    onOpenProtectionSetup = onOpenProtectionSetup
                )
            }

            item {
                CallHistoryCard(onOpenCallHistory)
            }
            item {
                PlanStatusCard(
                    tier = uiState.tier,
                    status = uiState.status,
                    hasPendingPurchase = uiState.hasPendingPurchase,
                    onOpenPaywall = onOpenPaywall
                )
            }

            item {
                WhitelistLimitCard(
                    count = whitelistCount,
                    limit = uiState.whitelistLimit,
                    progress = progress,
                    onOpenWhitelist = onOpenWhitelist
                )
            }

            item {
                LockedContactsCard(
                    hasPaidPlan = uiState.hasPaidPlan,
                    useContactsAutomatically = uiState.useContactsAutomatically,
                    importedContactsCount = uiState.importedContactsCount,
                    onOpenPaywall = onOpenPaywall,
                    onOpenContactsConsent = onOpenContactsConsent
                )
            }

            item {
                PrivacyCard()
            }
        }
    }
}

@Composable
private fun HomeHeader(
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.home_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProtectionStatusCard(
    isProtectionEnabled: Boolean,
    onOpenProtectionSetup: () -> Unit
) {
    // Same structure as PlanStatusCard: header row, full-width action below.
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
            HomeCardHeader(
                icon = Icons.Outlined.Security,
                title = if (isProtectionEnabled) {
                    stringResource(R.string.protection_status_active)
                } else {
                    stringResource(R.string.protection_status_not_configured)
                },
                description = stringResource(R.string.protection_whitelist_prompt)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenProtectionSetup,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isProtectionEnabled) {
                        stringResource(R.string.protection_status_active)
                    } else {
                        stringResource(R.string.activate_protection)
                    }
                )
            }
        }
    }
}

@Composable
private fun PlanStatusCard(
    tier: Tier,
    status: EntitlementStatus,
    hasPendingPurchase: Boolean,
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
            HomeCardHeader(
                icon = Icons.Outlined.CreditCard,
                title = stringResource(R.string.plan_current, tierDisplayName(tier)),
                description = when {
                    tier == Tier.FREE -> stringResource(R.string.plan_description_free)
                    status == EntitlementStatus.CANCELED_ACTIVE -> stringResource(R.string.plan_not_renewing)
                    else -> stringResource(R.string.plan_description_premium)
                }
            )

            if (hasPendingPurchase) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.paywall_pending),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tier == Tier.FREE) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onOpenPaywall,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.view_premium))
                }
            }
        }
    }
}

@Composable
private fun DowngradeNoticeCard(
    previousTier: Tier,
    currentTier: Tier,
    onOpenPaywall: () -> Unit,
    onDismiss: () -> Unit
) {
    val before = PlanFeatures.of(previousTier)
    val now = PlanFeatures.of(currentTier)

    val warning = CallGuardTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = warning.warningContainer,
            contentColor = warning.onWarningContainer
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = warning.onWarningContainer,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(
                        R.string.downgrade_title,
                        tierDisplayName(previousTier),
                        tierDisplayName(currentTier)
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.downgrade_whitelist_kept),
                style = MaterialTheme.typography.bodyMedium
            )

            if (before.contactsAllowed && !now.contactsAllowed) {
                Text(
                    text = stringResource(R.string.downgrade_contacts_off),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (now.historyRetentionDays < before.historyRetentionDays) {
                Text(
                    text = stringResource(R.string.downgrade_history_shorter, now.historyRetentionDays),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenPaywall, shape = RoundedCornerShape(14.dp)) {
                    Text(stringResource(R.string.downgrade_see_plans))
                }
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = warning.onWarningContainer)
                ) {
                    Text(stringResource(R.string.downgrade_dismiss))
                }
            }
        }
    }
}

@Composable
private fun WhitelistLimitCard(
    count: Int,
    /** Null = unlimited plan. */
    limit: Int?,
    progress: Float,
    onOpenWhitelist: () -> Unit
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
            HomeCardHeader(
                icon = Icons.Outlined.PhoneLocked,
                title = stringResource(R.string.whitelist_title),
                description = limit?.let { stringResource(R.string.whitelist_usage, count, it) }
                    ?: pluralStringResource(R.plurals.whitelist_count_unlimited, count, count)
            )

            if (limit != null) {
                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress },
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
            }

            Spacer(modifier = Modifier.height(14.dp))

            FilledTonalButton(
                onClick = onOpenWhitelist,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.manage_whitelist))
            }
        }
    }
}

@Composable
private fun LockedContactsCard(
    hasPaidPlan: Boolean,
    useContactsAutomatically: Boolean,
    importedContactsCount: Int,
    onOpenPaywall: () -> Unit,
    onOpenContactsConsent: () -> Unit
) {
    val description = when {
        !hasPaidPlan -> {
            stringResource(R.string.contacts_auto_description_disabled)
        }

        useContactsAutomatically -> {
            stringResource(R.string.contacts_imported_count, importedContactsCount)
        }

        else -> {
            stringResource(R.string.contacts_auto_description_enabled)
        }
    }

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
            HomeCardHeader(
                icon = Icons.Outlined.ContactPhone,
                title = stringResource(R.string.contacts_automatic),
                description = description
            )

            Spacer(modifier = Modifier.height(14.dp))

            when {
                !hasPaidPlan -> {
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
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(stringResource(R.string.unlock_with_premium))
                    }
                }

                !useContactsAutomatically -> {
                    Button(
                        onClick = onOpenContactsConsent,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContactPhone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(stringResource(R.string.enable_auto_contacts))
                    }
                }

                else -> {
                    FilledTonalButton(
                        onClick = onOpenContactsConsent,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.update_contacts))
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            HomeCardHeader(
                icon = Icons.Outlined.VerifiedUser,
                title = stringResource(R.string.privacy_title),
                description = stringResource(R.string.privacy_description)
            )
        }
    }
}

@Composable
private fun HomeCardHeader(
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
            Box(contentAlignment = Alignment.Center) {
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

@Composable
private fun CallHistoryCard(
    onOpenCallHistory: () -> Unit
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
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
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
                        text = stringResource(R.string.history_recent),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.history_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FilledTonalButton(
                onClick = onOpenCallHistory,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.view_history))
            }
        }
    }
}