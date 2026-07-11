package com.lsp.callguard.ui.screen.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onBlockUnknownChange: (Boolean) -> Unit,
    onBlockPrivateNumbersChange: (Boolean) -> Unit,
    onUseContactsAutomaticallyChange: (Boolean) -> Unit,
    onMockLicenseChange: (Boolean) -> Unit
) {
    val isLicensed = uiState.isLicensed
    val blockUnknown = uiState.appSettings.blockUnknown
    val blockPrivateNumbers = uiState.appSettings.blockPrivateNumbers
    val useContactsAutomatically = uiState.appSettings.useContactsAutomatically

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
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                SettingsHeader(onBack = onBack)
            }

            item {
                SettingsSectionTitle(stringResource(R.string.section_account))
            }

            item {
                LicenseCard(
                    isLicensed = isLicensed,
                    onOpenPaywall = onOpenPaywall,
                    onMockLicenseChange = onMockLicenseChange
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.section_protection))
            }

            item {
                ProtectionSettingsCard(
                    blockUnknown = blockUnknown,
                    onBlockUnknownChange = onBlockUnknownChange,
                    blockPrivateNumbers = blockPrivateNumbers,
                    onBlockPrivateNumbersChange = onBlockPrivateNumbersChange
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.section_whitelist_contacts))
            }

            item {
                ContactsSettingsCard(
                    isLicensed = isLicensed,
                    useContactsAutomatically = useContactsAutomatically,
                    onUseContactsAutomaticallyChange = onUseContactsAutomaticallyChange,
                    onOpenPaywall = onOpenPaywall
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.section_privacy))
            }

            item {
                PrivacySettingsCard(
                    onOpenTerms = onOpenTerms,
                    onOpenPrivacyPolicy = onOpenPrivacyPolicy
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.section_app))
            }

            item {
                AppSettingsCard(
                    onOpenLanguage = onOpenLanguage
                )
            }

            item {
                AppInfoCard()
            }
        }
    }
}

@Composable
private fun SettingsHeader(
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
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun LicenseCard(
    isLicensed: Boolean,
    onOpenPaywall: () -> Unit,
    onMockLicenseChange: (Boolean) -> Unit
) {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.WorkspacePremium,
            title = if (isLicensed) stringResource(R.string.app_name) + " Premium" else stringResource(R.string.plan_free),
            description = if (isLicensed) {
                stringResource(R.string.license_active)
            } else {
                stringResource(R.string.license_free)
            }
        )

        if (!isLicensed) {
            Spacer(modifier = Modifier.height(14.dp))

            FilledTonalButton(
                onClick = onOpenPaywall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(stringResource(R.string.view_premium))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoText(
            icon = Icons.Outlined.CreditCard,
            text = stringResource(R.string.subscription_benefits)
        )

        SettingsDivider()

        SettingsSwitchRow(
            icon = Icons.Outlined.WorkspacePremium,
            title = stringResource(R.string.license_mock_mode),
            description = stringResource(R.string.license_mock_mode_description),
            checked = isLicensed,
            onCheckedChange = onMockLicenseChange
        )
    }
}

@Composable
private fun ProtectionSettingsCard(
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
private fun ContactsSettingsCard(
    isLicensed: Boolean,
    useContactsAutomatically: Boolean,
    onUseContactsAutomaticallyChange: (Boolean) -> Unit,
    onOpenPaywall: () -> Unit
) {
    SettingsCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.use_contacts_automatically),
            description = if (isLicensed) {
                stringResource(R.string.contacts_auto_description_enabled)
            } else {
                stringResource(R.string.contacts_auto_description_disabled)
            },
            checked = useContactsAutomatically,
            enabled = isLicensed,
            onCheckedChange = onUseContactsAutomaticallyChange
        )

        if (!isLicensed) {
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onOpenPaywall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
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
private fun PrivacySettingsCard(
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
private fun AppSettingsCard(
    onOpenLanguage: () -> Unit
) {
    SettingsCard {
        SettingsActionRow(
            icon = Icons.Outlined.Language,
            title = stringResource(R.string.language),
            description = stringResource(R.string.change_language),
            onClick = onOpenLanguage
        )

        SettingsDivider()

        SettingsItemHeader(
            icon = Icons.Outlined.NotificationsOff,
            title = stringResource(R.string.no_ads),
            description = stringResource(R.string.privacy_description)
        )
    }
}

@Composable
private fun AppInfoCard() {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.Settings,
            title = stringResource(R.string.app_name),
            description = stringResource(R.string.app_version)
        )
    }
}

@Composable
private fun SettingsCard(
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
private fun SettingsActionRow(
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
private fun SettingsItemHeader(
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
private fun SettingsInfoText(
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
private fun SettingsIcon(
    icon: ImageVector
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
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
}

@Composable
private fun SettingsDivider() {
    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(14.dp))
}