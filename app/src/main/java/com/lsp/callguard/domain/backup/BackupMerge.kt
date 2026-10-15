package com.lsp.callguard.domain.backup

import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer

data class LocalEntry(val id: String, val label: String, val phoneE164: String, val updatedAt: Long)

data class WhitelistMerge(
    val toInsert: List<BackupEntry>,
    /** Local id → entry with the newer label. */
    val toUpdate: Map<String, BackupEntry>
)

/**
 * Combines imported whitelist entries with the local ones by canonical number (feature 001).
 * Never deletes: an exported file carries no removals. Unparseable imported numbers are ignored.
 */
fun mergeWhitelist(
    local: List<LocalEntry>,
    imported: List<BackupEntry>,
    normalizer: PhoneNormalizer,
    defaultRegion: String?
): WhitelistMerge {
    val localByPhone = local.associateBy { it.phoneE164 }
    val toInsert = linkedMapOf<String, BackupEntry>()
    val toUpdate = linkedMapOf<String, BackupEntry>()

    for (entry in imported) {
        val canonical = (normalizer.normalize(entry.phoneE164, defaultRegion) as? PhoneNormalizationResult.Valid)
            ?.phoneE164
            ?: continue
        val normalizedEntry = entry.copy(phoneE164 = canonical)
        val existing = localByPhone[canonical]

        when {
            existing == null -> {
                val already = toInsert[canonical]
                if (already == null || normalizedEntry.updatedAt > already.updatedAt) {
                    toInsert[canonical] = normalizedEntry
                }
            }
            normalizedEntry.updatedAt > existing.updatedAt && normalizedEntry.label != existing.label -> {
                toUpdate[existing.id] = normalizedEntry
            }
        }
    }

    return WhitelistMerge(toInsert.values.toList(), toUpdate)
}
