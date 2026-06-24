package com.lsp.callguard.data.local.preferences


import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsp.callguard.domain.model.SubscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionPreferences(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) {
    private object Keys {
        val IS_SUBSCRIBED = booleanPreferencesKey("is_subscribed")
        val PLAN_NAME = stringPreferencesKey("plan_name")
    }

    val subscriptionFlow: Flow<SubscriptionState> =
        dataStore.data.map { preferences ->
            SubscriptionState(
                isSubscribed = preferences[Keys.IS_SUBSCRIBED] ?: false,
                planName = preferences[Keys.PLAN_NAME] ?: "Free"
            )
        }

    suspend fun setMockSubscriptionActive(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_SUBSCRIBED] = value
            preferences[Keys.PLAN_NAME] = if (value) "Premium" else "Free"
        }
    }
}