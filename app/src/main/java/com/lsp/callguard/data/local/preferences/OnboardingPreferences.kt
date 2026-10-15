package com.lsp.callguard.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsp.callguard.domain.model.OnboardingProgress
import kotlinx.coroutines.flow.first

class OnboardingPreferences(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val ONBOARDING_PROGRESS = stringPreferencesKey("onboarding_progress")
    }

    /** Null when never written (fresh install or a version before this key existed). */
    suspend fun progress(): OnboardingProgress? {
        return parse(dataStore.data.first()[Keys.ONBOARDING_PROGRESS])
    }

    /** Stores [progress] unless the saved progress is already the same or further along. */
    suspend fun advanceTo(progress: OnboardingProgress) {
        dataStore.edit { preferences ->
            val current = parse(preferences[Keys.ONBOARDING_PROGRESS])

            if (current == null || progress.ordinal > current.ordinal) {
                preferences[Keys.ONBOARDING_PROGRESS] = progress.name
            }
        }
    }

    private fun parse(value: String?): OnboardingProgress? {
        return value?.let { stored -> OnboardingProgress.entries.find { it.name == stored } }
    }
}
