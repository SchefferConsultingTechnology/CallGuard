package com.lsp.callguard.data.local.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lsp.callguard.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPreferences(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) {
    private object Keys {
        val BLOCK_UNKNOWN = booleanPreferencesKey("block_unknown")
        val BLOCK_PRIVATE_NUMBERS = booleanPreferencesKey("block_private_numbers")
        val USE_CONTACTS_AUTOMATICALLY = booleanPreferencesKey("use_contacts_automatically")
    }

    val settingsFlow: Flow<AppSettings> =
        dataStore.data.map { preferences ->
            AppSettings(
                blockUnknown = preferences[Keys.BLOCK_UNKNOWN] ?: true,
                blockPrivateNumbers = preferences[Keys.BLOCK_PRIVATE_NUMBERS] ?: true,
                useContactsAutomatically = preferences[Keys.USE_CONTACTS_AUTOMATICALLY] ?: false
            )
        }

    suspend fun setBlockUnknown(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.BLOCK_UNKNOWN] = value
        }
    }

    suspend fun setBlockPrivateNumbers(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.BLOCK_PRIVATE_NUMBERS] = value
        }
    }

    suspend fun setUseContactsAutomatically(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.USE_CONTACTS_AUTOMATICALLY] = value
        }
    }
}
