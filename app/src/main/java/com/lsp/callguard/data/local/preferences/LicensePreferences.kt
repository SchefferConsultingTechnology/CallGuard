package com.lsp.callguard.data.local.preferences


import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsp.callguard.domain.model.LicenseState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LicensePreferences(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) {
    private object Keys {
        val IS_LICENSED = booleanPreferencesKey("is_licensed")
        val PLAN_NAME = stringPreferencesKey("plan_name")
    }

    val licenseFlow: Flow<LicenseState> =
        dataStore.data.map { preferences ->
            LicenseState(
                isLicensed = preferences[Keys.IS_LICENSED] ?: false,
                planName = preferences[Keys.PLAN_NAME] ?: "Free"
            )
        }

    suspend fun setLicenseActive(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_LICENSED] = value
            preferences[Keys.PLAN_NAME] = if (value) "Premium" else "Free"
        }
    }

    /** Debug-only override, kept separate so its intent is unambiguous at call sites. */
    suspend fun setMockLicenseActive(value: Boolean) {
        setLicenseActive(value)
    }
}