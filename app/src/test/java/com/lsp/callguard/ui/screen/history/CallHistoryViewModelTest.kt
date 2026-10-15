package com.lsp.callguard.ui.screen.history

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.repository.CallDecisionLogRepository
import com.lsp.callguard.data.repository.WhitelistRepository
import com.lsp.callguard.domain.model.Tier
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.PhoneNumberDisplayFormatter
import com.lsp.callguard.fakes.FakeAllowedNumberDao
import com.lsp.callguard.fakes.FakeCallDecisionLogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CallHistoryViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()

    private val blocked = "+5511987654321"

    private fun log(id: String, phone: String?, normalized: String?, allowed: Boolean) =
        CallDecisionLogEntity(id, phone, normalized, allowed, "X", createdAt = id.hashCode().toLong())

    private val logs = listOf(
        log("blocked", "11987654321", blocked, allowed = false),
        log("legacy", "011 3333-4444", null, allowed = false),
        log("hidden", null, null, allowed = false),
        log("unparseable", "12345", null, allowed = false),
        log("allowed", "+5521999998888", "+5521999998888", allowed = true)
    )

    private lateinit var whitelistDao: FakeAllowedNumberDao
    private lateinit var license: LicensePreferences
    private lateinit var viewModel: CallHistoryViewModel

    private fun allowed(phone: String, id: String = phone) =
        AllowedNumberEntity(id, id, phone, createdAt = 0, updatedAt = 0)

    private fun setUp(whitelist: List<AllowedNumberEntity> = emptyList()) {
        whitelistDao = FakeAllowedNumberDao(whitelist)
        license = LicensePreferences(
            PreferenceDataStoreFactory.create { File(tempFolder.root, "license.preferences_pb") }
        )
        viewModel = CallHistoryViewModel(
            repository = CallDecisionLogRepository(FakeCallDecisionLogDao(logs), PhoneNormalizer()),
            whitelistRepository = WhitelistRepository(whitelistDao, fallbackLabel = "No name"),
            licensePreferences = license,
            normalizer = PhoneNormalizer(),
            formatter = PhoneNumberDisplayFormatter(),
            regionProvider = { "BR" }
        )
    }

    /** DataStore reads run on real IO threads, outside runTest's virtual clock. */
    private suspend fun awaitUntil(condition: () -> Boolean) {
        withContext(Dispatchers.Default) {
            withTimeout(5_000) { while (!condition()) delay(10) }
        }
    }

    @Before
    fun main() = Dispatchers.setMain(dispatcher)

    @After
    fun reset() = Dispatchers.resetMain()

    @Test
    fun `actions per entry`() = runTest(dispatcher) {
        setUp()
        backgroundScope.launch { viewModel.uiState.collect {} }

        val actions = viewModel.uiState.value.actions
        assertEquals(QuickAllowAction.ADD, actions["blocked"])
        assertEquals(QuickAllowAction.ADD, actions["legacy"])
        assertEquals(QuickAllowAction.NONE, actions["hidden"])
        assertEquals(QuickAllowAction.NONE, actions["unparseable"])
        assertEquals(QuickAllowAction.NONE, actions["allowed"])
        assertEquals("+551133334444", viewModel.uiState.value.canonicalPhones["legacy"])
    }

    @Test
    fun `whitelisted number shows as in whitelist`() = runTest(dispatcher) {
        setUp(listOf(allowed(blocked)))
        backgroundScope.launch { viewModel.uiState.collect {} }

        assertEquals(QuickAllowAction.IN_WHITELIST, viewModel.uiState.value.actions["blocked"])
    }

    @Test
    fun `add then undo`() = runTest(dispatcher) {
        setUp()
        val events = mutableListOf<HistoryEvent>()
        backgroundScope.launch { viewModel.events.collect { events += it } }

        viewModel.addToWhitelist(blocked, "Clínica")
        awaitUntil { events.isNotEmpty() }
        assertEquals(listOf(blocked), whitelistDao.current.map { it.phoneE164 })
        assertEquals("Clínica", whitelistDao.current.single().label)
        assertEquals(listOf<HistoryEvent>(HistoryEvent.Added(blocked)), events)

        viewModel.undoAdd(blocked)
        awaitUntil { whitelistDao.current.isEmpty() }
        assertEquals(emptyList<AllowedNumberEntity>(), whitelistDao.current)
    }

    @Test
    fun `free limit keeps the addition pending until upgrade`() = runTest(dispatcher) {
        setUp((1..5).map { allowed("+55119000000$it") })
        val events = mutableListOf<HistoryEvent>()
        backgroundScope.launch { viewModel.events.collect { events += it } }

        viewModel.addToWhitelist(blocked, "Clínica")
        awaitUntil { events.isNotEmpty() }
        assertEquals(listOf<HistoryEvent>(HistoryEvent.LimitReached), events)
        assertEquals(5, whitelistDao.current.size)

        license.setMockTier(Tier.PLUS)
        awaitUntil { events.size == 2 }
        assertEquals(6, whitelistDao.current.size)
        assertEquals(HistoryEvent.AddedAfterUpgrade(blocked), events.last())
    }
}
