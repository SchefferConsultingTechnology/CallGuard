package com.lsp.callguard.ui.screen.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.repository.AddAllowedNumberResult
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.model.FREE_WHITELIST_LIMIT
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WhitelistItemUi(
    val id: String,
    val label: String,
    val phoneE164: String,
    val displayPhone: String
)

data class WhitelistUiState(
    val numbers: List<WhitelistItemUi> = emptyList(),
    val count: Int = 0,
    val limit: Int = FREE_WHITELIST_LIMIT,
    val isLicensed: Boolean = false
) {
    val progress: Float
        get() = count / limit.toFloat()

    val reachedLimit: Boolean
        get() = !isLicensed && count >= limit
}

sealed interface PhoneInputPreview {
    data object Empty : PhoneInputPreview
    data object Invalid : PhoneInputPreview
    data class Valid(val phoneE164: String, val display: String) : PhoneInputPreview
}

sealed class WhitelistEvent {
    data object OpenLimitDialog : WhitelistEvent()
    data object NumberAlreadyExists : WhitelistEvent()
    data object InvalidNumber : WhitelistEvent()
}

class WhitelistViewModel(
    private val repository: WhitelistRepository,
    private val licensePreferences: LicensePreferences,
    private val normalizer: PhoneNormalizer,
    private val formatter: PhoneNumberDisplayFormatter,
    private val regionProvider: RegionProvider
) : ViewModel() {

    /** Loaded once so typing in the dialog can be previewed synchronously. */
    private val region = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            region.value = regionProvider.defaultRegion()
        }
    }

    val uiState: StateFlow<WhitelistUiState> =
        combine(
            repository.observeAllowedNumbers(),
            repository.observeCount(),
            licensePreferences.licenseFlow,
            region
        ) { numbers, count, license, userRegion ->
            WhitelistUiState(
                numbers = numbers.map { number ->
                    WhitelistItemUi(
                        id = number.id,
                        label = number.label,
                        phoneE164 = number.phoneE164,
                        displayPhone = formatter.format(number.phoneE164, userRegion)
                    )
                },
                count = count,
                isLicensed = license.isLicensed
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WhitelistUiState()
        )

    val phoneExample: StateFlow<String?> =
        region
            .map { formatter.exampleFor(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = formatter.exampleFor(region.value)
            )

    private val _events = MutableSharedFlow<WhitelistEvent>()
    val events = _events.asSharedFlow()

    fun previewFor(input: String): PhoneInputPreview {
        if (input.isBlank()) return PhoneInputPreview.Empty

        return when (val result = normalizer.normalize(input, region.value)) {
            is PhoneNormalizationResult.Valid -> PhoneInputPreview.Valid(
                phoneE164 = result.phoneE164,
                display = formatter.format(result.phoneE164, userRegion = null)
            )

            is PhoneNormalizationResult.Invalid -> PhoneInputPreview.Invalid
        }
    }

    fun addNumber(label: String, rawInput: String) {
        val state = uiState.value

        if (state.reachedLimit) {
            viewModelScope.launch {
                _events.emit(WhitelistEvent.OpenLimitDialog)
            }
            return
        }

        val preview = previewFor(rawInput)

        if (preview !is PhoneInputPreview.Valid) {
            viewModelScope.launch {
                _events.emit(WhitelistEvent.InvalidNumber)
            }
            return
        }

        viewModelScope.launch {
            when (repository.addNumber(label, preview.phoneE164)) {
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
