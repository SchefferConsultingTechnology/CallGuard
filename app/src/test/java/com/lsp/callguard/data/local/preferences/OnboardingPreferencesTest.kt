package com.lsp.callguard.data.local.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsp.callguard.domain.model.OnboardingProgress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class OnboardingPreferencesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dataStore by lazy {
        PreferenceDataStoreFactory.create { File(tempFolder.root, "onboarding.preferences_pb") }
    }

    private val preferences by lazy { OnboardingPreferences(dataStore) }

    @Test
    fun `missing key reads as null`() = runTest {
        assertNull(preferences.progress())
    }

    @Test
    fun `advancing is persisted`() = runTest {
        preferences.advanceTo(OnboardingProgress.ONBOARDING_DONE)
        assertEquals(OnboardingProgress.ONBOARDING_DONE, preferences.progress())

        preferences.advanceTo(OnboardingProgress.COMPLETED)
        assertEquals(OnboardingProgress.COMPLETED, preferences.progress())
    }

    @Test
    fun `progress never goes back`() = runTest {
        preferences.advanceTo(OnboardingProgress.COMPLETED)
        preferences.advanceTo(OnboardingProgress.NOT_STARTED)
        preferences.advanceTo(OnboardingProgress.ONBOARDING_DONE)

        assertEquals(OnboardingProgress.COMPLETED, preferences.progress())
    }

    @Test
    fun `unknown stored value reads as null`() = runTest {
        dataStore.edit { it[stringPreferencesKey("onboarding_progress")] = "SOMETHING_ELSE" }

        assertNull(preferences.progress())
    }
}
