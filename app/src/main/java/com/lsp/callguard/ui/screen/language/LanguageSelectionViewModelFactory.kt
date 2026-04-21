package com.lsp.callguard.ui.screen.language



import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore

@Composable
fun rememberLanguageSelectionViewModel(
    languagePreferences: LanguagePreferences
): LanguageSelectionViewModel {

    val context = LocalContext.current.applicationContext

    val factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LanguageSelectionViewModel(languagePreferences) as T
            }
        }
    }

    return viewModel(factory = factory)
}