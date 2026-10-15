package com.lsp.callguard.data.backup

import com.lsp.callguard.core.language.AppLanguage
import com.lsp.callguard.data.local.dao.AllowedNumberDao
import com.lsp.callguard.data.local.dao.CallDecisionLogDao
import com.lsp.callguard.data.local.entity.AllowedNumberEntity
import com.lsp.callguard.data.local.entity.CallDecisionLogEntity
import com.lsp.callguard.data.local.preferences.LanguagePreferences
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.domain.backup.BackupCipher
import com.lsp.callguard.domain.backup.BackupCodec
import com.lsp.callguard.domain.backup.BackupData
import com.lsp.callguard.domain.backup.BackupEntry
import com.lsp.callguard.domain.backup.BackupError
import com.lsp.callguard.domain.backup.BackupException
import com.lsp.callguard.domain.backup.BackupLog
import com.lsp.callguard.domain.backup.BackupSettings
import com.lsp.callguard.domain.backup.LocalEntry
import com.lsp.callguard.domain.backup.mergeWhitelist
import com.lsp.callguard.domain.billing.historyCutoff
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.RegionProvider
import kotlinx.coroutines.flow.first
import java.util.UUID

sealed interface ImportResult {
    data class Imported(
        val added: Int,
        val updated: Int,
        val historyAdded: Int,
        /** Language restored from the file, for the caller to apply. */
        val language: AppLanguage?
    ) : ImportResult

    data class Failed(val error: BackupError) : ImportResult
}

/** Builds and applies password-protected backup files (feature 005, US4). */
class BackupRepository(
    private val allowedNumberDao: AllowedNumberDao,
    private val callDecisionLogDao: CallDecisionLogDao,
    private val settingsPreferences: SettingsPreferences,
    private val languagePreferences: LanguagePreferences,
    private val licensePreferences: LicensePreferences,
    private val normalizer: PhoneNormalizer,
    private val regionProvider: RegionProvider,
    private val clock: () -> Long = System::currentTimeMillis
) {

    suspend fun export(password: String, includeHistory: Boolean): ByteArray {
        val settings = settingsPreferences.settingsFlow.first()

        val data = BackupData(
            exportedAt = clock(),
            whitelist = allowedNumberDao.getAll().map {
                BackupEntry(label = it.label, phoneE164 = it.phoneE164, updatedAt = it.updatedAt)
            },
            settings = BackupSettings(
                blockUnknown = settings.blockUnknown,
                blockPrivateNumbers = settings.blockPrivateNumbers,
                language = languagePreferences.selectedLanguageFlow.first()?.code
            ),
            history = if (includeHistory) {
                callDecisionLogDao.observeLatest().first().map {
                    BackupLog(it.id, it.phone, it.normalizedPhone, it.allowed, it.reason, it.createdAt)
                }
            } else {
                emptyList()
            }
        )

        return BackupCipher.encrypt(BackupCodec.encode(data), password)
    }

    /** Nothing local is changed unless the file opens with [password] and is fully read. */
    suspend fun import(file: ByteArray, password: String): ImportResult {
        val data = try {
            BackupCodec.decode(BackupCipher.decrypt(file, password))
        } catch (e: BackupException) {
            return ImportResult.Failed(e.error)
        }

        val now = clock()
        val local = allowedNumberDao.getAll().map { LocalEntry(it.id, it.label, it.phoneE164, it.updatedAt) }
        val merge = mergeWhitelist(local, data.whitelist, normalizer, regionProvider.defaultRegion())

        merge.toInsert.forEach { entry ->
            allowedNumberDao.insert(
                AllowedNumberEntity(
                    id = UUID.randomUUID().toString(),
                    label = entry.label,
                    phoneE164 = entry.phoneE164,
                    createdAt = now,
                    updatedAt = entry.updatedAt
                )
            )
        }
        merge.toUpdate.forEach { (id, entry) -> allowedNumberDao.updateLabel(id, entry.label, entry.updatedAt) }

        settingsPreferences.setBlockUnknown(data.settings.blockUnknown)
        settingsPreferences.setBlockPrivateNumbers(data.settings.blockPrivateNumbers)

        val language = AppLanguage.fromCode(data.settings.language)
        language?.let { languagePreferences.saveSelectedLanguage(it) }

        val historyAdded = importHistory(data.history, now)

        return ImportResult.Imported(
            added = merge.toInsert.size,
            updated = merge.toUpdate.size,
            historyAdded = historyAdded,
            language = language
        )
    }

    private suspend fun importHistory(history: List<BackupLog>, now: Long): Int {
        if (history.isEmpty()) return 0

        val existingIds = callDecisionLogDao.observeLatest().first().mapTo(mutableSetOf()) { it.id }
        val cutoff = historyCutoff(now, licensePreferences.licenseFlow.first().tier)
        val toAdd = history.filter { it.id !in existingIds && it.createdAt >= cutoff }

        toAdd.forEach {
            callDecisionLogDao.insert(
                CallDecisionLogEntity(it.id, it.phone, it.normalizedPhone, it.allowed, it.reason, it.createdAt)
            )
        }
        return toAdd.size
    }
}
