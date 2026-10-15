package com.lsp.callguard.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.repository.AddAllowedNumberResult
import com.lsp.callguard.data.repository.CallDecisionLogRepository
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.model.CallDecisionLog
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.PhoneNumberDisplayFormatter
import com.lsp.callguard.domain.phone.RegionProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CallHistoryUiState(
    val logs: List<CallDecisionLog> = emptyList(),
    /** Readable number per log id; raw value when it could not be interpreted, null when hidden. */
    val displayPhones: Map<String, String?> = emptyMap(),
    val actions: Map<String, QuickAllowAction> = emptyMap(),
    /** Number per log id in the single identity used by call screening (feature 001). */
    val canonicalPhones: Map<String, String?> = emptyMap()
)

sealed interface HistoryEvent {
    data class Added(val phoneE164: String) : HistoryEvent
    data object LimitReached : HistoryEvent
    data class AddedAfterUpgrade(val phoneE164: String) : HistoryEvent
}

class CallHistoryViewModel(
    private val repository: CallDecisionLogRepository,
    private val whitelistRepository: WhitelistRepository,
    private val licensePreferences: LicensePreferences,
    private val normalizer: PhoneNormalizer,
    private val formatter: PhoneNumberDisplayFormatter,
    private val regionProvider: RegionProvider
) : ViewModel() {

    private val region = MutableStateFlow<String?>(null)

    private val _events = MutableSharedFlow<HistoryEvent>()
    val events = _events.asSharedFlow()

    /** Number the user chose while the plan was full; added once the plan allows it (FR-007). */
    private var pendingAddition: PendingAddition? = null

    init {
        viewModelScope.launch {
            region.value = regionProvider.defaultRegion()
        }

        viewModelScope.launch {
            licensePreferences.licenseFlow.collect {
                val pending = pendingAddition ?: return@collect
                if (hasRoomInWhitelist()) {
                    pendingAddition = null
                    if (add(pending.phoneE164, pending.label)) {
                        _events.emit(HistoryEvent.AddedAfterUpgrade(pending.phoneE164))
                    }
                }
            }
        }
    }

    val uiState: StateFlow<CallHistoryUiState> =
        combine(
            repository.observeLatestLogs(),
            whitelistRepository.observeAllowedNumbers(),
            region
        ) { logs, allowedNumbers, userRegion ->
            val inWhitelist = allowedNumbers.mapTo(mutableSetOf()) { it.phoneE164 }
            val canonical = logs.associate { log -> log.id to canonicalOf(log, userRegion) }

            CallHistoryUiState(
                logs = logs,
                displayPhones = logs.associate { log ->
                    log.id to formatter.formatOrRaw(canonical[log.id], log.phone, userRegion)
                },
                actions = logs.associate { log ->
                    log.id to quickAllowAction(log.allowed, canonical[log.id], inWhitelist)
                },
                canonicalPhones = canonical
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CallHistoryUiState()
        )

    /** Rows written before feature 001 may lack the canonical number; re-read with the same rule. */
    private fun canonicalOf(log: CallDecisionLog, userRegion: String?): String? {
        log.normalizedPhone?.let { return it }
        return (normalizer.normalize(log.phone, userRegion) as? PhoneNormalizationResult.Valid)?.phoneE164
    }

    fun addToWhitelist(phoneE164: String, label: String) {
        viewModelScope.launch {
            if (!hasRoomInWhitelist()) {
                pendingAddition = PendingAddition(phoneE164, label)
                _events.emit(HistoryEvent.LimitReached)
                return@launch
            }

            if (add(phoneE164, label)) {
                _events.emit(HistoryEvent.Added(phoneE164))
            }
        }
    }

    fun undoAdd(phoneE164: String) {
        viewModelScope.launch {
            whitelistRepository.deleteByPhone(phoneE164)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clear()
        }
    }

    private suspend fun add(phoneE164: String, label: String): Boolean {
        return whitelistRepository.addNumber(label, phoneE164) == AddAllowedNumberResult.Success
    }

    private suspend fun hasRoomInWhitelist(): Boolean {
        val limit = licensePreferences.licenseFlow.first().features.whitelistLimit ?: return true
        return whitelistRepository.observeCount().first() < limit
    }

    private data class PendingAddition(val phoneE164: String, val label: String)
}
