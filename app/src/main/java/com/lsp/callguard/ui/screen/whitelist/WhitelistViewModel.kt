package com.lsp.callguard.ui.screen.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.SubscriptionPreferences
import com.lsp.callguard.data.repository.AddAllowedNumberResult
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.model.AllowedNumber
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val FREE_WHITELIST_LIMIT = 5

data class WhitelistUiState(
    val numbers: List<AllowedNumber> = emptyList(),
    val count: Int = 0,
    val limit: Int = FREE_WHITELIST_LIMIT,
    val isSubscribed: Boolean = false
) {
    val progress: Float
        get() = count / limit.toFloat()

    val reachedLimit: Boolean
        get() = !isSubscribed && count >= limit
}

sealed class WhitelistEvent {
    data object OpenLimitDialog : WhitelistEvent()
    data object NumberAlreadyExists : WhitelistEvent()
}



class WhitelistViewModel(
    private val repository: WhitelistRepository,
    private val subscriptionPreferences: SubscriptionPreferences
) : ViewModel() {

    val uiState: StateFlow<WhitelistUiState> =
        combine(
            repository.observeAllowedNumbers(),
            repository.observeCount(),
            subscriptionPreferences.subscriptionFlow
        ) { numbers, count ,subscription ->
            WhitelistUiState(
                numbers = numbers,
                count = count,
                isSubscribed = subscription.isSubscribed
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WhitelistUiState()
        )

    private val _events = MutableSharedFlow<WhitelistEvent>()
    val events = _events.asSharedFlow()

    fun addNumber(label: String, phoneE164: String) {
        val state = uiState.value

        if (state.reachedLimit) {
            viewModelScope.launch {
                _events.emit(WhitelistEvent.OpenLimitDialog)
            }
            return
        }

        viewModelScope.launch {
            when (repository.addNumber(label, phoneE164)) {
                AddAllowedNumberResult.Success -> Unit

                AddAllowedNumberResult.AlreadyExists -> {
                    _events.emit(WhitelistEvent.NumberAlreadyExists)
                }
            }
        }
    }

    fun deleteNumber(id: String) {
        viewModelScope.launch {
            repository.deleteNumber(id)
        }
    }
}

