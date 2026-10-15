package com.lsp.callguard.ui.screen.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.BuildConfig
import com.lsp.callguard.data.backup.BackupRepository
import com.lsp.callguard.data.backup.ImportResult
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.data.billing.RestoreResult
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.domain.backup.BackupError
import com.lsp.callguard.domain.model.AppSettings
import com.lsp.callguard.domain.model.Tier
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BackupEvent {
    data object Exported : BackupEvent
    data object ExportFailed : BackupEvent
    data class Imported(val result: ImportResult) : BackupEvent
}

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val tier: Tier = Tier.FREE
) {
    val hasPaidPlan: Boolean
        get() = tier != Tier.FREE
}

class SettingsViewModel(
    private val settingsPreferences: SettingsPreferences,
    private val licensePreferences: LicensePreferences,
    private val billingRepository: BillingRepository,
    private val backupRepository: BackupRepository,
    private val writeFile: suspend (Uri, ByteArray) -> Unit,
    private val readFile: suspend (Uri) -> ByteArray
) : ViewModel() {

    private val _backupEvents = MutableSharedFlow<BackupEvent>()
    val backupEvents = _backupEvents.asSharedFlow()

    /** Chosen in the dialog before the system file picker opens. */
    private var pendingExport: Pair<String, Boolean>? = null

    fun prepareExport(password: String, includeHistory: Boolean) {
        pendingExport = password to includeHistory
    }

    fun exportTo(uri: Uri) {
        val (password, includeHistory) = pendingExport ?: return
        pendingExport = null
        viewModelScope.launch {
            val event = try {
                writeFile(uri, backupRepository.export(password, includeHistory))
                BackupEvent.Exported
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                BackupEvent.ExportFailed
            }
            _backupEvents.emit(event)
        }
    }

    fun importFrom(uri: Uri, password: String) {
        viewModelScope.launch {
            val event = try {
                BackupEvent.Imported(backupRepository.import(readFile(uri), password))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                BackupEvent.Imported(ImportResult.Failed(BackupError.Unreadable))
            }
            _backupEvents.emit(event)
        }
    }

    private val _restoreResults = MutableSharedFlow<RestoreResult>()
    val restoreResults = _restoreResults.asSharedFlow()

    fun restorePurchases() {
        viewModelScope.launch {
            _restoreResults.emit(billingRepository.restorePurchases())
        }
    }


    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsPreferences.settingsFlow,
            licensePreferences.licenseFlow
        ) { settings, license ->
            SettingsUiState(
                appSettings = settings,
                tier = license.tier
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    /** Debug builds only: lets the UI be exercised for each tier without the store. */
    fun setMockTier(tier: Tier) {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            licensePreferences.setMockTier(tier)
        }
    }
    fun setBlockUnknown(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setBlockUnknown(value)
        }
    }

    fun setBlockPrivateNumbers(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setBlockPrivateNumbers(value)
        }
    }

    fun setUseContactsAutomatically(value: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setUseContactsAutomatically(value)
        }
    }
}