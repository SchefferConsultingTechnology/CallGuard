package com.lsp.callguard.domain.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test

class BackupCipherTest {

    private val plain = "+5511987654321 Mãe".toByteArray()
    private val password = "segredo123"

    @Test
    fun `round trip`() = assertArrayEquals(plain, BackupCipher.decrypt(BackupCipher.encrypt(plain, password), password))

    @Test
    fun `wrong password is refused`() =
        assertError(BackupError.WrongPasswordOrCorrupted) { BackupCipher.decrypt(BackupCipher.encrypt(plain, password), "outra-senha") }

    @Test
    fun `tampered byte is refused`() {
        val file = BackupCipher.encrypt(plain, password)
        file[file.size - 3] = (file[file.size - 3].toInt() xor 0x01).toByte()
        assertError(BackupError.WrongPasswordOrCorrupted) { BackupCipher.decrypt(file, password) }
    }

    @Test
    fun `foreign file is unreadable`() {
        assertError(BackupError.Unreadable) { BackupCipher.decrypt("not a backup at all, no magic".toByteArray(), password) }
        assertError(BackupError.Unreadable) { BackupCipher.decrypt(ByteArray(3), password) }
    }

    @Test
    fun `same input encrypts differently and hides the content`() {
        val a = BackupCipher.encrypt(plain, password)
        val b = BackupCipher.encrypt(plain, password)
        assertFalse(a.contentEquals(b))
        assertFalse(String(a, Charsets.ISO_8859_1).contains("5511987654321"))
    }

    private fun assertError(expected: BackupError, block: () -> Unit) {
        try {
            block()
            fail("expected $expected")
        } catch (e: BackupException) {
            assertEquals(expected, e.error)
        }
    }
}
