package com.lsp.callguard.ui.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lsp.callguard.BuildConfig
import com.lsp.callguard.R
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.ui.components.tierDisplayName

@Composable
internal fun LicenseCard(
    tier: Tier,
    onOpenPaywall: () -> Unit,
    onRestorePurchases: () -> Unit,
    onMockTierChange: (Tier) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.WorkspacePremium,
            title = stringResource(R.string.plan_current, tierDisplayName(tier)),
            description = if (tier != Tier.FREE) {
                stringResource(R.string.license_active)
            } else {
                stringResource(R.string.license_free)
            }
        )

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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onRestorePurchases) {
                Text(stringResource(R.string.paywall_restore))
            }

            if (tier != Tier.FREE) {
                TextButton(onClick = { uriHandler.openUri(BillingRepository.manageSubscriptionUrl(null)) }) {
                    Text(stringResource(R.string.manage_subscription))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoText(
            icon = Icons.Outlined.CreditCard,
            text = stringResource(R.string.subscription_benefits)
        )

        if (BuildConfig.DEBUG) {
            SettingsDivider()

            Text(
                text = stringResource(R.string.license_mock_mode),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.license_mock_mode_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tier.entries.forEach { option ->
                    FilterChip(
                        selected = option == tier,
                        onClick = { onMockTierChange(option) },
                        label = { Text(tierDisplayName(option)) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProtectionSettingsCard(
    blockUnknown: Boolean,
    onBlockUnknownChange: (Boolean) -> Unit,
    blockPrivateNumbers: Boolean,
    onBlockPrivateNumbersChange: (Boolean) -> Unit
) {
    SettingsCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Security,
            title = stringResource(R.string.block_unknown_numbers),
            description = stringResource(R.string.block_unknown_description),
            checked = blockUnknown,
            onCheckedChange = onBlockUnknownChange
        )

        SettingsDivider()

        SettingsSwitchRow(
            icon = Icons.Outlined.Block,
            title = stringResource(R.string.block_private_numbers),
            description = stringResource(R.string.block_private_description),
            checked = blockPrivateNumbers,
            onCheckedChange = onBlockPrivateNumbersChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoText(
            icon = Icons.Outlined.Info,
            text = stringResource(R.string.settings_applied_when_enabled)
        )
    }
}

@Composable
internal fun ContactsSettingsCard(
    hasPaidPlan: Boolean,
    useContactsAutomatically: Boolean,
    onUseContactsAutomaticallyChange: (Boolean) -> Unit,
    onOpenPaywall: () -> Unit
) {
    SettingsCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.use_contacts_automatically),
            description = if (hasPaidPlan) {
                stringResource(R.string.contacts_auto_description_enabled)
            } else {
                stringResource(R.string.contacts_auto_description_disabled)
            },
            checked = useContactsAutomatically,
            enabled = hasPaidPlan,
            onCheckedChange = onUseContactsAutomaticallyChange
        )

        if (!hasPaidPlan) {
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenPaywall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.unlock_contacts_premium))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoText(
            icon = Icons.Outlined.PrivacyTip,
            text = stringResource(R.string.contacts_access_policy)
        )
    }
}

@Composable
internal fun PrivacySettingsCard(
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.VerifiedUser,
            title = stringResource(R.string.privacy_first),
            description = stringResource(R.string.privacy_first_description)
        )

        SettingsDivider()

        SettingsActionRow(
            icon = Icons.Outlined.Policy,
            title = stringResource(R.string.terms_of_use),
            description = stringResource(R.string.terms_description),
            onClick = onOpenTerms
        )

        SettingsDivider()

        SettingsActionRow(
            icon = Icons.Outlined.PrivacyTip,
            title = stringResource(R.string.privacy_policy),
            description = stringResource(R.string.privacy_policy_description),
            onClick = onOpenPrivacyPolicy
        )
    }
}

@Composable
internal fun AppInfoCard() {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.Settings,
            title = stringResource(R.string.app_name),
            description = stringResource(R.string.app_version, BuildConfig.VERSION_NAME)
        )
    }
}

@Composable
internal fun SettingsCard(
    content: @Composable () -> Unit
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
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon = icon)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
internal fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIcon(icon = icon)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
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
}

@Composable
internal fun SettingsItemHeader(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        SettingsIcon(icon = icon)

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
internal fun SettingsInfoText(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun SettingsIcon(
    icon: ImageVector
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(38.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
internal fun SettingsDivider() {
    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(14.dp))
}