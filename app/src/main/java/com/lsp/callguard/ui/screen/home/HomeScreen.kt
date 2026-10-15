package com.lsp.callguard.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.PhoneLocked
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R
import com.lsp.callguard.core.theme.CallGuardTheme
import com.lsp.callguard.domain.billing.EntitlementStatus
import com.lsp.callguard.domain.model.PlanFeatures
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.ui.components.tierDisplayName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenWhitelist: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenProtectionSetup: () -> Unit,
    onOpenCallHistory: () -> Unit,
    onDismissDowngradeNotice: () -> Unit,
    reselection: Flow<Unit> = emptyFlow()
) {
    val listState = rememberLazyListState()
    LaunchedEffect(reselection) {
        reselection.collect { listState.animateScrollToItem(0) }
    }

    // Dashboard (feature 007): fits one screen; whitelist, history and settings are tabs.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding: PaddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp)
        ) {
            item {
                HomeHeader()
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WhitelistSummaryCard(
                        count = uiState.whitelistCount,
                        limit = uiState.whitelistLimit,
                        progress = uiState.progress,
                        onClick = onOpenWhitelist,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    PlanSummaryCard(
                        tier = uiState.tier,
                        status = uiState.status,
                        hasPendingPurchase = uiState.hasPendingPurchase,
                        onClick = onOpenPaywall,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            item {
                RecentCallsCard(
                    recentCalls = uiState.recentCalls,
                    onOpenCallHistory = onOpenCallHistory
                )
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
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
private fun SummaryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SummaryCardTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WhitelistSummaryCard(
    count: Int,
    /** Null = unlimited plan. */
    limit: Int?,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SummaryCard(onClick = onClick, modifier = modifier) {
        SummaryCardTitle(icon = Icons.Outlined.PhoneLocked, title = stringResource(R.string.tab_whitelist))

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = limit?.let { stringResource(R.string.home_whitelist_short, count, it) }
                ?: pluralStringResource(R.plurals.home_whitelist_count_short, count, count),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (limit != null) {
            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (count >= limit) {
                    CallGuardTheme.extendedColors.warning
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun PlanSummaryCard(
    tier: Tier,
    status: EntitlementStatus,
    hasPendingPurchase: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SummaryCard(onClick = onClick, modifier = modifier) {
        SummaryCardTitle(icon = Icons.Outlined.CreditCard, title = stringResource(R.string.home_plan_title))

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = tierDisplayName(tier),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = when {
                hasPendingPurchase -> stringResource(R.string.home_plan_pending)
                status == EntitlementStatus.CANCELED_ACTIVE -> stringResource(R.string.home_plan_not_renewing)
                tier == Tier.FREE -> stringResource(R.string.view_premium) + " ›"
                else -> stringResource(R.string.home_plan_manage) + " ›"
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecentCallsCard(
    recentCalls: List<RecentCall>,
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
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_recent_calls),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onOpenCallHistory) {
                    Text(stringResource(R.string.home_see_all))
                }
            }

            if (recentCalls.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_no_calls_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            } else {
                recentCalls.forEach { call ->
                    RecentCallRow(call = call, onClick = onOpenCallHistory)
                }
            }
        }
    }
}

@Composable
private fun RecentCallRow(call: RecentCall, onClick: () -> Unit) {
    // Colour is never the only signal: icon and status text differ too (spec 006, FR-008).
    val statusColor = if (call.allowed) CallGuardTheme.extendedColors.success else MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (call.allowed) Icons.Outlined.Verified else Icons.Outlined.Block,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.displayPhone?.takeIf { it.isNotBlank() } ?: stringResource(R.string.private_number),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(if (call.allowed) R.string.status_allowed else R.string.status_blocked),
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }

        Text(
            text = formatCallTime(call.createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Today → time only; earlier → short date, both in the app's language. */
private fun formatCallTime(createdAt: Long): String {
    val zone = ZoneId.systemDefault()
    val moment = Instant.ofEpochMilli(createdAt).atZone(zone)
    val formatter = if (moment.toLocalDate() == LocalDate.now(zone)) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    } else {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    }
    return formatter.withLocale(Locale.getDefault()).format(moment)
}

