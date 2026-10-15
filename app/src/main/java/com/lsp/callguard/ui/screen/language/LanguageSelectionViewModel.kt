package com.lsp.callguard.ui.screen.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.core.language.AppLanguage
import com.lsp.callguard.core.language.LocaleManagerHelper
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LanguageSelectionUiState(
    val selectedLanguage: AppLanguage? = null
)

class LanguageSelectionViewModel(
    private val languagePreferences: LanguagePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LanguageSelectionUiState(selectedLanguage = LocaleManagerHelper.displayedLanguage())
    )
    val uiState: StateFlow<LanguageSelectionUiState> = _uiState.asStateFlow()

    /** Applies on tap, like the onboarding; the activity is recreated in the new language. */
    fun onLanguageSelected(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)

        viewModelScope.launch {
            languagePreferences.saveSelectedLanguage(language)
            LocaleManagerHelper.applyLanguage(language)
        }
    }
}
