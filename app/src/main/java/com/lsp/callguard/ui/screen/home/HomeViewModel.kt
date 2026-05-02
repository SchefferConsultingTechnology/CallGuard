package com.lsp.callguard.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.repository.WhitelistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

private const val FREE_WHITELIST_LIMIT = 5

data class HomeUiState(
    val whitelistCount: Int = 0,
    val whitelistLimit: Int = FREE_WHITELIST_LIMIT,
    val isSubscribed: Boolean = false
) {
    val progress: Float
        get() = whitelistCount / whitelistLimit.toFloat()
}

class HomeViewModel(
    repository: WhitelistRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        repository.observeCount()
            .map { count ->
                HomeUiState(
                    whitelistCount = count
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )
}

