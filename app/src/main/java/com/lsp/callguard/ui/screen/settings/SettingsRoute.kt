package com.lsp.callguard.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.lsp.callguard.core.language.LocaleManagerHelper
import com.lsp.callguard.data.backup.ImportResult
import com.lsp.callguard.data.billing.RestoreResult
import com.lsp.callguard.domain.backup.BackupError
import com.lsp.callguard.ui.components.tierDisplayName
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Ajustes tab: list of categories (feature 007). */
@Composable
fun SettingsListRoute(
    viewModel: SettingsViewModel,
    onOpenCategory: (SettingsCategory) -> Unit,
    reselection: Flow<Unit> = emptyFlow()
) {
    val state by viewModel.uiState.collectAsState()
    val uiState = state.copy(language = LocaleManagerHelper.currentLanguage())

    SettingsListScreen(
        uiState = uiState,
        onOpenCategory = onOpenCategory,
        reselection = reselection
    )
}

/** One Ajustes page. Launchers, dialogs and messages live on the page that owns them. */
@Composable
fun SettingsCategoryRoute(
    category: SettingsCategory,
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenContactsConsent: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val uiState = state.copy(language = LocaleManagerHelper.currentLanguage())
    val snackbarHostState = remember { SnackbarHostState() }

    var showExportDialog by remember { mutableStateOf(false) }
    var importUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let(viewModel::exportTo) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> importUri = uri }

    when (category) {
        SettingsCategory.PLAN -> RestoreMessages(viewModel, uiState, snackbarHostState)
        SettingsCategory.BACKUP -> BackupMessages(viewModel, snackbarHostState)
        else -> Unit
    }

    SettingsCategoryScreen(
        category = category,
        uiState = uiState,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        actions = SettingsCategoryActions(
            onOpenPaywall = onOpenPaywall,
            onOpenLanguage = onOpenLanguage,
            onOpenTerms = onOpenTerms,
            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
            onRestorePurchases = viewModel::restorePurchases,
            onMockTierChange = viewModel::setMockTier,
            onBlockUnknownChange = viewModel::setBlockUnknown,
            onBlockPrivateNumbersChange = viewModel::setBlockPrivateNumbers,
            // Turning contacts on always goes through consent and the permission (constitution II);
            // turning them off is immediate.
            onUseContactsAutomaticallyChange = { enabled ->
                if (enabled) onOpenContactsConsent() else viewModel.setUseContactsAutomatically(false)
            },
            onExportBackup = { showExportDialog = true },
            onImportBackup = { importLauncher.launch(arrayOf("*/*")) },
            onThemeModeChange = viewModel::setThemeMode
        )
    )

    if (showExportDialog) {
        ExportBackupDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { password, includeHistory ->
                showExportDialog = false
                viewModel.prepareExport(password, includeHistory)
                exportLauncher.launch("callguard-${LocalDate.now()}.cgbk")
            }
        )
    }

    importUri?.let { uri ->
        ImportBackupDialog(
            onDismiss = { importUri = null },
            onConfirm = { password ->
                importUri = null
                viewModel.importFrom(uri, password)
            }
        )
    }
}

@Composable
private fun RestoreMessages(
    viewModel: SettingsViewModel,
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState
) {
    val restoredMessage = stringResource(R.string.paywall_restore_done, tierDisplayName(uiState.tier))
    val nothingMessage = stringResource(R.string.paywall_restore_none)
    val unavailableMessage = stringResource(R.string.paywall_store_unavailable)

    LaunchedEffect(Unit) {
        viewModel.restoreResults.collect { result ->
            snackbarHostState.showSnackbar(
                when (result) {
                    is RestoreResult.Restored -> restoredMessage
                    RestoreResult.NothingFound -> nothingMessage
                    RestoreResult.StoreUnavailable -> unavailableMessage
                }
            )
        }
    }
}

@Composable
private fun BackupMessages(
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val exportedMessage = stringResource(R.string.backup_exported)
    val exportFailedMessage = stringResource(R.string.backup_export_failed)
    val wrongPasswordMessage = stringResource(R.string.backup_wrong_password)
    val newerMessage = stringResource(R.string.backup_newer_format)
    val unreadableMessage = stringResource(R.string.backup_unreadable)
    val importedTemplate = stringResource(R.string.backup_imported)

    LaunchedEffect(Unit) {
        viewModel.backupEvents.collect { event ->
            val message = when (event) {
                BackupEvent.Exported -> exportedMessage
                BackupEvent.ExportFailed -> exportFailedMessage
                is BackupEvent.Imported -> when (val result = event.result) {
                    // Language and theme are applied by the view model once this was read.
                    is ImportResult.Imported ->
                        importedTemplate.format(result.added, result.updated, result.historyAdded)
                    is ImportResult.Failed -> when (result.error) {
                        BackupError.WrongPasswordOrCorrupted -> wrongPasswordMessage
                        BackupError.NewerFormat -> newerMessage
                        BackupError.Unreadable -> unreadableMessage
                    }
                }
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}
