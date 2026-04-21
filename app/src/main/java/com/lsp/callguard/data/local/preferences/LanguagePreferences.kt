package com.lsp.callguard.data.local.preferences

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsp.callguard.core.language.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LanguagePreferences(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) {

    private object Keys {
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    val selectedLanguageFlow: Flow<AppLanguage?> =
        dataStore.data.map { preferences ->
            AppLanguage.fromCode(preferences[Keys.SELECTED_LANGUAGE])
        }

    suspend fun saveSelectedLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[Keys.SELECTED_LANGUAGE] = language.code
        }
    }
}