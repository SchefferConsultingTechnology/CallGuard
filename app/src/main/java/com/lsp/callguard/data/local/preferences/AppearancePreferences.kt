package com.lsp.callguard.data.local.preferences

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsp.callguard.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AppearancePreferences(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    /** An unreadable preference falls back to following the system. */
    val themeModeFlow: Flow<ThemeMode> =
        dataStore.data
            .map { preferences -> ThemeMode.fromStorage(preferences[Keys.THEME_MODE]) }
            .catch { emit(ThemeMode.SYSTEM) }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.storageValue
        }
    }
}
