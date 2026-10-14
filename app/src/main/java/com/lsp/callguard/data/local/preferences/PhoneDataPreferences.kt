package com.lsp.callguard.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class PhoneDataPreferences(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val PHONE_NORMALIZATION_VERSION = intPreferencesKey("phone_normalization_version")
        val LAST_KNOWN_REGION = stringPreferencesKey("last_known_region")
    }

    suspend fun phoneNormalizationVersion(): Int {
        return dataStore.data.first()[Keys.PHONE_NORMALIZATION_VERSION] ?: 0
    }

    suspend fun setPhoneNormalizationVersion(version: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.PHONE_NORMALIZATION_VERSION] = version
        }
    }

    suspend fun lastKnownRegion(): String? {
        return dataStore.data.first()[Keys.LAST_KNOWN_REGION]
    }

    suspend fun setLastKnownRegion(region: String) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_KNOWN_REGION] = region
        }
    }
}
