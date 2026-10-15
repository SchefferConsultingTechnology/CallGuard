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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lsp.callguard.R
import com.lsp.callguard.core.language.LocaleManagerHelper
import com.lsp.callguard.core.theme.ThemeModeApplier
import com.lsp.callguard.data.backup.ImportResult
import com.lsp.callguard.data.billing.RestoreResult
import com.lsp.callguard.domain.backup.BackupError
import com.lsp.callguard.ui.components.tierDisplayName
import java.time.LocalDate

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit) {
    val viewModel = rememberSettingsViewModel()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val restoredMessage = stringResource(R.string.paywall_restore_done, tierDisplayName(uiState.tier))
    val nothingMessage = stringResource(R.string.paywall_restore_none)
    val unavailableMessage = stringResource(R.string.paywall_store_unavailable)

    var showExportDialog by remember { mutableStateOf(false) }
    var importUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let(viewModel::exportTo) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> importUri = uri }

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
                    is ImportResult.Imported -> {
                        result.language?.let(LocaleManagerHelper::applyLanguage)
                        result.themeMode?.let { ThemeModeApplier.apply(context, it) }
                        importedTemplate.format(result.added, result.updated, result.historyAdded)
                    }
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

    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenPaywall = onOpenPaywall,
        onOpenLanguage = onOpenLanguage,
        onOpenTerms = onOpenTerms,
        onOpenPrivacyPolicy = onOpenPrivacyPolicy,
        onBlockUnknownChange = viewModel::setBlockUnknown,
        onBlockPrivateNumbersChange = viewModel::setBlockPrivateNumbers,
        onUseContactsAutomaticallyChange = viewModel::setUseContactsAutomatically,
        onMockTierChange = viewModel::setMockTier,
        onThemeModeChange = viewModel::setThemeMode,
        onRestorePurchases = viewModel::restorePurchases,
        onExportBackup = { showExportDialog = true },
        onImportBackup = { importLauncher.launch(arrayOf("*/*")) },
        snackbarHostState = snackbarHostState
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
