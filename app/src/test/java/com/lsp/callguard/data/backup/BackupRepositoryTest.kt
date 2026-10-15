package com.lsp.callguard.data.backup

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lsp.callguard.core.language.AppLanguage
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import com.lsp.callguard.data.local.preferences.AppearancePreferences
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.domain.backup.BackupError
import com.lsp.callguard.domain.model.ThemeMode
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.fakes.FakeAllowedNumberDao
import com.lsp.callguard.fakes.FakeCallDecisionLogDao
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val now = 1_800_000_000_000L
    private val password = "segredo123"

    private inner class Device(name: String, whitelist: List<AllowedNumberEntity> = emptyList(), logs: List<CallDecisionLogEntity> = emptyList()) {
        val allowedDao = FakeAllowedNumberDao(whitelist)
        val logDao = FakeCallDecisionLogDao(logs)
        private val store = PreferenceDataStoreFactory.create { File(tempFolder.root, "$name.preferences_pb") }
        val settings = SettingsPreferences(store)
        val language = LanguagePreferences(store)
        val appearance = AppearancePreferences(store)
        val repository = BackupRepository(
            allowedNumberDao = allowedDao,
            callDecisionLogDao = logDao,
            settingsPreferences = settings,
            languagePreferences = language,
            appearancePreferences = appearance,
            licensePreferences = LicensePreferences(store),
            normalizer = PhoneNormalizer(),
            regionProvider = { "BR" },
            clock = { now }
        )
    }

    private fun entry(id: String, label: String, phone: String, updatedAt: Long = 1) =
        AllowedNumberEntity(id, label, phone, createdAt = 0, updatedAt = updatedAt)

    private fun log(id: String, ageDays: Int) =
        CallDecisionLogEntity(id, "11987654321", "+5511987654321", false, "BLOCKED_NOT_IN_WHITELIST", now - ageDays * 86_400_000L)

    @Test
    fun `export on one device and import on another restores whitelist and settings`() = runTest {
        val a = Device("a", whitelist = listOf(entry("1", "Mãe", "+5511987654321"), entry("2", "Clínica", "+551133334444")))
        a.settings.setBlockPrivateNumbers(false)
        a.language.saveSelectedLanguage(AppLanguage.ES_ES)
        a.appearance.setThemeMode(ThemeMode.DARK)
        val file = a.repository.export(password, includeHistory = false)

        val b = Device("b")
        val result = b.repository.import(file, password)

        assertEquals(ImportResult.Imported(added = 2, updated = 0, historyAdded = 0, language = AppLanguage.ES_ES, themeMode = ThemeMode.DARK), result)
        assertEquals(setOf("+5511987654321", "+551133334444"), b.allowedDao.current.map { it.phoneE164 }.toSet())
        assertEquals(false, b.settings.settingsFlow.first().blockPrivateNumbers)
        assertEquals(AppLanguage.ES_ES, b.language.selectedLanguageFlow.first())
        assertEquals(ThemeMode.DARK, b.appearance.themeModeFlow.first())
    }

    @Test
    fun `importing twice does not duplicate`() = runTest {
        val a = Device("a", whitelist = listOf(entry("1", "Mãe", "+5511987654321")))
        val file = a.repository.export(password, includeHistory = true)
        val b = Device("b")

        b.repository.import(file, password)
        val second = b.repository.import(file, password)

        assertEquals(0, (second as ImportResult.Imported).added)
        assertEquals(1, b.allowedDao.current.size)
    }

    @Test
    fun `wrong password changes nothing`() = runTest {
        val a = Device("a", whitelist = listOf(entry("1", "Mãe", "+5511987654321")))
        val file = a.repository.export(password, includeHistory = false)
        val b = Device("b", whitelist = listOf(entry("x", "Pai", "+5521999998888")))

        val result = b.repository.import(file, "senha-errada")

        assertEquals(ImportResult.Failed(BackupError.WrongPasswordOrCorrupted), result)
        assertEquals(listOf("+5521999998888"), b.allowedDao.current.map { it.phoneE164 })
    }

    @Test
    fun `history only travels when included and respects the plan retention`() = runTest {
        val a = Device("a", logs = listOf(log("recent", 2), log("old", 30)))

        val withoutHistory = Device("b").let { b ->
            b.repository.import(a.repository.export(password, includeHistory = false), password)
            b.logDao.observeLatest().first()
        }
        assertTrue(withoutHistory.isEmpty())

        val c = Device("c")
        val result = c.repository.import(a.repository.export(password, includeHistory = true), password)
        // Free plan keeps 7 days: the 30-day-old entry is not restored.
        assertEquals(1, (result as ImportResult.Imported).historyAdded)
        assertEquals(listOf("recent"), c.logDao.observeLatest().first().map { it.id })
    }

    @Test
    fun `newer label from the file updates the local one`() = runTest {
        val a = Device("a", whitelist = listOf(entry("1", "Mãe (celular)", "+5511987654321", updatedAt = 50)))
        val b = Device("b", whitelist = listOf(entry("9", "Mae", "+5511987654321", updatedAt = 10)))

        val result = b.repository.import(a.repository.export(password, includeHistory = false), password)

        assertEquals(1, (result as ImportResult.Imported).updated)
        assertEquals("Mãe (celular)", b.allowedDao.current.single().label)
    }
}
