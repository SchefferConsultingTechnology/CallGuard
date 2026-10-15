package com.lsp.callguard.domain.model

/** The person's theme choice (spec 006, FR-010). [storageValue] is what DataStore and backups keep. */
enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        /** Missing or unknown values follow the system. */
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM

        /** Null when the value is not a known mode (e.g. a backup from a future version). */
        fun fromStorageOrNull(value: String?): ThemeMode? =
            entries.firstOrNull { it.storageValue == value }
    }
}
