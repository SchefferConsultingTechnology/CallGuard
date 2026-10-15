package com.lsp.callguard.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lsp.callguard.R
import com.lsp.callguard.domain.model.ThemeMode
import com.lsp.callguard.domain.model.Tier

/** Actions a category page can trigger; each page uses only its own. */
internal class SettingsCategoryActions(
    val onOpenPaywall: () -> Unit,
    val onOpenLanguage: () -> Unit,
    val onOpenTerms: () -> Unit,
    val onOpenPrivacyPolicy: () -> Unit,
    val onRestorePurchases: () -> Unit,
    val onMockTierChange: (Tier) -> Unit,
    val onBlockUnknownChange: (Boolean) -> Unit,
    val onBlockPrivateNumbersChange: (Boolean) -> Unit,
    val onUseContactsAutomaticallyChange: (Boolean) -> Unit,
    val onExportBackup: () -> Unit,
    val onImportBackup: () -> Unit,
    val onThemeModeChange: (ThemeMode) -> Unit
)

/** One Ajustes page (feature 007): the cards that used to be a section of the long settings list. */
@Composable
internal fun SettingsCategoryScreen(
    category: SettingsCategory,
    uiState: SettingsUiState,
    actions: SettingsCategoryActions,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
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
                SettingsPageHeader(title = stringResource(category.title), onBack = onBack)
            }

            item {
                CategoryContent(category, uiState, actions)
            }
        }
    }
}

@Composable
private fun CategoryContent(
    category: SettingsCategory,
    uiState: SettingsUiState,
    actions: SettingsCategoryActions
) = Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    when (category) {
        SettingsCategory.PLAN -> LicenseCard(
            tier = uiState.tier,
            onOpenPaywall = actions.onOpenPaywall,
            onRestorePurchases = actions.onRestorePurchases,
            onMockTierChange = actions.onMockTierChange
        )

        SettingsCategory.PROTECTION -> ProtectionSettingsCard(
            blockUnknown = uiState.appSettings.blockUnknown,
            onBlockUnknownChange = actions.onBlockUnknownChange,
            blockPrivateNumbers = uiState.appSettings.blockPrivateNumbers,
            onBlockPrivateNumbersChange = actions.onBlockPrivateNumbersChange
        )

        SettingsCategory.CONTACTS -> ContactsSettingsCard(
            hasPaidPlan = uiState.hasPaidPlan,
            useContactsAutomatically = uiState.appSettings.useContactsAutomatically,
            onUseContactsAutomaticallyChange = actions.onUseContactsAutomaticallyChange,
            onOpenPaywall = actions.onOpenPaywall
        )

        SettingsCategory.BACKUP -> BackupCard(
            onExport = actions.onExportBackup,
            onImport = actions.onImportBackup
        )

        SettingsCategory.APPEARANCE -> {
            AppearanceCard(
                selected = uiState.themeMode,
                onSelect = actions.onThemeModeChange
            )
            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.language),
                    description = uiState.language?.title ?: stringResource(R.string.summary_language_device),
                    onClick = actions.onOpenLanguage
                )
            }
        }

        SettingsCategory.PRIVACY -> PrivacySettingsCard(
            onOpenTerms = actions.onOpenTerms,
            onOpenPrivacyPolicy = actions.onOpenPrivacyPolicy
        )
    }
}
