package com.lsp.callguard.ui.screen.paywall

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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R
import com.lsp.callguard.data.billing.StoreOffer
import com.lsp.callguard.domain.model.PlanFeatures
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.ui.components.tierDisplayName

@Composable
fun PaywallScreen(
    uiState: PaywallUiState,
    snackbarHostState: SnackbarHostState,
    onSelectPeriod: (BillingPeriod) -> Unit,
    onBuy: (StoreOffer) -> Unit,
    onRetry: () -> Unit,
    onRestore: () -> Unit,
    onContinueFreeClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedButton(
                    onClick = onContinueFreeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.paywallscreen_bottom_bar_free))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
        ) {
            item { PaywallHeader() }

            if (uiState.pendingPurchase) {
                item { NoticeCard(text = stringResource(R.string.paywall_pending)) }
            }

            if (uiState.storeUnavailable) {
                item {
                    NoticeCard(
                        text = stringResource(R.string.paywall_store_unavailable),
                        actionLabel = stringResource(R.string.paywall_retry),
                        onAction = onRetry
                    )
                }
            }

            item {
                PeriodSelector(
                    selected = uiState.period,
                    onSelect = onSelectPeriod
                )
            }

            item {
                PlanCard(
                    tier = Tier.FREE,
                    isCurrent = uiState.currentTier == Tier.FREE,
                    priceText = stringResource(R.string.paywall_free_price),
                    offer = null,
                    onBuy = onBuy
                )
            }

            item {
                PlanCard(
                    tier = Tier.PLUS,
                    isCurrent = uiState.currentTier == Tier.PLUS,
                    priceText = priceText(uiState.plusOffer, uiState.period),
                    offer = uiState.plusOffer.takeIf { uiState.currentTier < Tier.PLUS },
                    onBuy = onBuy,
                    lifetimeOffer = uiState.plusLifetime.takeIf { uiState.currentTier < Tier.PLUS }
                )
            }

            item {
                PlanCard(
                    tier = Tier.PRO,
                    isCurrent = uiState.currentTier == Tier.PRO,
                    // Not on sale yet: the "coming soon" badge says it; no price line.
                    priceText = if (uiState.proOnSale) priceText(uiState.proOffer, uiState.period) else null,
                    offer = uiState.proOffer.takeIf { uiState.proOnSale && uiState.currentTier < Tier.PRO },
                    onBuy = onBuy,
                    comingSoon = !uiState.proOnSale
                )
            }

            item { CommonFeaturesCard() }

            item {
                Text(
                    text = stringResource(R.string.paywall_terms),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                TextButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.paywall_restore))
                }
            }
        }
    }
}

@Composable
private fun priceText(offer: StoreOffer?, period: BillingPeriod): String {
    if (offer == null) return stringResource(R.string.paywall_price_unavailable)

    return if (period == BillingPeriod.MONTHLY) {
        stringResource(R.string.paywall_per_month, offer.formattedPrice)
    } else {
        stringResource(R.string.paywall_per_year, offer.formattedPrice)
    }
}

@Composable
private fun PaywallHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.paywallscreen_header_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.paywallscreen_header_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PeriodSelector(
    selected: BillingPeriod,
    onSelect: (BillingPeriod) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        FilterChip(
            selected = selected == BillingPeriod.MONTHLY,
            onClick = { onSelect(BillingPeriod.MONTHLY) },
            label = { Text(stringResource(R.string.paywall_period_monthly)) }
        )
        FilterChip(
            selected = selected == BillingPeriod.ANNUAL,
            onClick = { onSelect(BillingPeriod.ANNUAL) },
            label = { Text(stringResource(R.string.paywall_period_annual)) }
        )
    }
}

@Composable
private fun PlanCard(
    tier: Tier,
    isCurrent: Boolean,
    priceText: String?,
    offer: StoreOffer?,
    onBuy: (StoreOffer) -> Unit,
    lifetimeOffer: StoreOffer? = null,
    comingSoon: Boolean = false
) {
    val highlight = tier == Tier.PLUS && !isCurrent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight || isCurrent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tierDisplayName(tier),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                when {
                    isCurrent -> Badge(stringResource(R.string.paywall_current_plan))
                    comingSoon -> Badge(stringResource(R.string.paywall_coming_soon))
                    offer?.hasFreeTrial == true -> Badge(stringResource(R.string.paywall_trial_badge))
                }
            }

            if (priceText != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PlanFeatureList(PlanFeatures.of(tier))

            if (offer != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onBuy(offer) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.paywall_subscribe, tierDisplayName(tier)))
                }
            }

            if (lifetimeOffer != null) {
                TextButton(
                    onClick = { onBuy(lifetimeOffer) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.paywall_lifetime, lifetimeOffer.formattedPrice))
                }
            }
        }
    }
}

@Composable
private fun PlanFeatureList(features: PlanFeatures) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FeatureLine(
            features.whitelistLimit?.let { stringResource(R.string.feature_whitelist_limited, it) }
                ?: stringResource(R.string.feature_whitelist_unlimited)
        )
        if (features.contactsAllowed) {
            FeatureLine(stringResource(R.string.feature_contacts))
        }
        FeatureLine(stringResource(R.string.feature_history_days, features.historyRetentionDays))
        if (features.driveBackup) {
            FeatureLine(stringResource(R.string.feature_drive_backup))
        }
    }
}

@Composable
private fun CommonFeaturesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.paywall_common_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FeatureLine(stringResource(R.string.paywall_common_blocking))
                FeatureLine(stringResource(R.string.paywall_common_no_ads))
            }
        }
    }
}

@Composable
private fun FeatureLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Badge(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun NoticeCard(
    text: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (actionLabel != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
