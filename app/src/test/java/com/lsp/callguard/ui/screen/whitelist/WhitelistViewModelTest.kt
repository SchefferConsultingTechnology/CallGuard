package com.lsp.callguard.ui.screen.whitelist

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.PhoneNumberDisplayFormatter
import com.lsp.callguard.fakes.FakeAllowedNumberDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class WhitelistViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()

    private val existing = AllowedNumberEntity(
        id = "mom",
        label = "Mãe",
        phoneE164 = "+5511987654321",
        createdAt = 0,
        updatedAt = 0
    )

    private lateinit var dao: FakeAllowedNumberDao
    private lateinit var viewModel: WhitelistViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        dao = FakeAllowedNumberDao(listOf(existing))
        viewModel = WhitelistViewModel(
            repository = WhitelistRepository(dao, fallbackLabel = "No name"),
            licensePreferences = LicensePreferences(
                PreferenceDataStoreFactory.create {
                    File(tempFolder.root, "license.preferences_pb")
                }
            ),
            normalizer = PhoneNormalizer(),
            formatter = PhoneNumberDisplayFormatter(),
            regionProvider = { "BR" }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `national input previews the international number`() {
        assertEquals(
            PhoneInputPreview.Valid(phoneE164 = "+5511987654321", display = "+55 11 98765-4321"),
            viewModel.previewFor("11 98765-4321")
        )
    }

    @Test
    fun `invalid and empty inputs`() {
        assertEquals(PhoneInputPreview.Invalid, viewModel.previewFor("123"))
        assertEquals(PhoneInputPreview.Empty, viewModel.previewFor(""))
        assertEquals(PhoneInputPreview.Empty, viewModel.previewFor("   "))
    }

    @Test
    fun `example number follows the region`() {
        assertNotNull(viewModel.phoneExample.value)
    }

    @Test
    fun `duplicate detected through the canonical number`() = runTest(dispatcher) {
        val events = mutableListOf<WhitelistEvent>()
        backgroundScope.launch { viewModel.events.collect { events += it } }

        viewModel.addNumber("Mãe", "(11) 98765-4321")

        assertEquals(listOf<WhitelistEvent>(WhitelistEvent.NumberAlreadyExists), events)
        assertEquals(listOf(existing), dao.current)
    }

    @Test
    fun `invalid number is rejected without inserting`() = runTest(dispatcher) {
        val events = mutableListOf<WhitelistEvent>()
        backgroundScope.launch { viewModel.events.collect { events += it } }

        viewModel.addNumber("x", "123")

        assertEquals(listOf<WhitelistEvent>(WhitelistEvent.InvalidNumber), events)
        assertEquals(listOf(existing), dao.current)
    }

    @Test
    fun `national number is stored canonically`() = runTest(dispatcher) {
        viewModel.addNumber("Tio", "(21) 99876-5432")

        assertEquals("+5521998765432", dao.current.last().phoneE164)
    }
}
