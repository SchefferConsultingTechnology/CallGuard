package com.lsp.callguard.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.repository.CallDecisionLogRepository
import com.lsp.callguard.domain.model.CallDecisionLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CallHistoryUiState(
    val logs: List<CallDecisionLog> = emptyList()
)

class CallHistoryViewModel(
    private val repository: CallDecisionLogRepository
) : ViewModel() {

    val uiState: StateFlow<CallHistoryUiState> =
        repository.observeLatestLogs()
            .map { logs ->
                CallHistoryUiState(logs = logs)
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