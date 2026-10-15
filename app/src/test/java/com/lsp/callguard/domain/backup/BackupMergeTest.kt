package com.lsp.callguard.domain.backup

import com.lsp.callguard.domain.phone.PhoneNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test

/** Mirrors the mergeWhitelist table in specs/005-drive-backup/contracts/backup-file.md. */
class BackupMergeTest {

    private val phone = "+5511987654321"
    private fun merge(local: List<LocalEntry>, imported: List<BackupEntry>) =
        mergeWhitelist(local, imported, PhoneNormalizer(), "BR")

    @Test
    fun `missing number is inserted`() {
        val result = merge(emptyList(), listOf(BackupEntry("Mãe", phone, 1)))
        assertEquals(listOf(BackupEntry("Mãe", phone, 1)), result.toInsert)
        assertEquals(emptyMap<String, BackupEntry>(), result.toUpdate)
    }

    @Test
    fun `newer label updates the local entry`() {
        val result = merge(listOf(LocalEntry("id1", "Mae", phone, 1)), listOf(BackupEntry("Mãe", phone, 2)))
        assertEquals(mapOf("id1" to BackupEntry("Mãe", phone, 2)), result.toUpdate)
        assertEquals(emptyList<BackupEntry>(), result.toInsert)
    }

    @Test
    fun `older label is ignored`() {
        val result = merge(listOf(LocalEntry("id1", "Mãe", phone, 5)), listOf(BackupEntry("Mae", phone, 2)))
        assertEquals(WhitelistMerge(emptyList(), emptyMap()), result)
    }

    @Test
    fun `legacy eight digit number matches the canonical one`() {
        val result = merge(listOf(LocalEntry("id1", "Mãe", phone, 5)), listOf(BackupEntry("Mãe", "+551187654321", 1)))
        assertEquals(WhitelistMerge(emptyList(), emptyMap()), result)
    }

    @Test
    fun `unparseable imported number is ignored`() {
        val result = merge(emptyList(), listOf(BackupEntry("x", "12345", 1)))
        assertEquals(WhitelistMerge(emptyList(), emptyMap()), result)
    }

    @Test
    fun `duplicates inside the file collapse to the newest`() {
        val result = merge(emptyList(), listOf(BackupEntry("A", phone, 1), BackupEntry("B", "011987654321", 3)))
        assertEquals(listOf(BackupEntry("B", phone, 3)), result.toInsert)
    }
}
