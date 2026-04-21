package com.lsp.callguard.ui.screen.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsp.callguard.core.language.AppLanguage
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LanguageSelectionUiState(
    val selectedLanguage: AppLanguage? = null,
    val isSaving: Boolean = false
)

class LanguageSelectionViewModel(
    private val languagePreferences: LanguagePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageSelectionUiState())
    val uiState: StateFlow<LanguageSelectionUiState> = _uiState.asStateFlow()

    fun onLanguageSelected(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun onConfirm(onSaved: () -> Unit) {
        val language = _uiState.value.selectedLanguage ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            languagePreferences.saveSelectedLanguage(language)
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSaved()
        }
    }
}

