package com.lsp.callguard.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.repository.CallDecisionLogRepository
import com.lsp.callguard.domain.model.CallDecisionLog
import com.lsp.callguard.domain.phone.PhoneNumberDisplayFormatter
import com.lsp.callguard.domain.phone.RegionProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CallHistoryUiState(
    val logs: List<CallDecisionLog> = emptyList(),
    /** Readable number per log id; raw value when it could not be interpreted, null when hidden. */
    val displayPhones: Map<String, String?> = emptyMap()
)

class CallHistoryViewModel(
    private val repository: CallDecisionLogRepository,
    private val formatter: PhoneNumberDisplayFormatter,
    private val regionProvider: RegionProvider
) : ViewModel() {

    val uiState: StateFlow<CallHistoryUiState> =
        combine(
            repository.observeLatestLogs(),
            flow { emit(regionProvider.defaultRegion()) }
        ) { logs, userRegion ->
            CallHistoryUiState(
                logs = logs,
                displayPhones = logs.associate { log ->
                    log.id to formatter.formatOrRaw(log.normalizedPhone, log.phone, userRegion)
                }
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CallHistoryUiState()
            )

    fun clearHistory() {
        viewModelScope.launch {
            repository.clear()
        }
    }
}