package com.lsp.callguard.domain.backup

/** v2 (feature 006) appends the theme choice at the end of the file. */
const val BACKUP_FORMAT_VERSION = 2

/** What a backup carries (constitution II): never the contacts cache, consent or the plan. */
data class BackupData(
    val formatVersion: Int = BACKUP_FORMAT_VERSION,
    val exportedAt: Long,
    val whitelist: List<BackupEntry>,
    val settings: BackupSettings,
    /** Empty unless the person chose to include it. */
    val history: List<BackupLog> = emptyList()
)

data class BackupEntry(
    val label: String,
    val phoneE164: String,
    val updatedAt: Long
)

data class BackupSettings(
    val blockUnknown: Boolean,
    val blockPrivateNumbers: Boolean,
    /** App language code (e.g. "pt-BR"), or null when it follows the device. */
    val language: String?,
    /** "system" | "light" | "dark"; null in v1 files, which leaves the device's choice untouched. */
    val themeMode: String? = null
)

data class BackupLog(
    val id: String,
    val phone: String?,
    val normalizedPhone: String?,
    val allowed: Boolean,
    val reason: String,
    val createdAt: Long
)

sealed interface BackupError {
    data object WrongPasswordOrCorrupted : BackupError
    /** Written by a newer app version; the app must be updated first. */
    data object NewerFormat : BackupError
    data object Unreadable : BackupError
}

class BackupException(val error: BackupError) : Exception(error.toString())
