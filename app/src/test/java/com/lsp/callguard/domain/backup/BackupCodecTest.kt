package com.lsp.callguard.domain.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class BackupCodecTest {

    private val data = BackupData(
        exportedAt = 1_800_000_000_000,
        whitelist = listOf(
            BackupEntry("Mãe", "+5511987654321", 10),
            BackupEntry("Clínica 🏥", "+351912345678", 20)
        ),
        settings = BackupSettings(blockUnknown = true, blockPrivateNumbers = false, language = "pt-BR"),
        history = listOf(
            BackupLog("a", "11987654321", "+5511987654321", allowed = true, reason = "ALLOWED_BY_WHITELIST", createdAt = 5),
            BackupLog("b", null, null, allowed = false, reason = "BLOCKED_PRIVATE_NUMBER", createdAt = 6)
        )
    )

    @Test
    fun `round trip with history`() = assertEquals(data, BackupCodec.decode(BackupCodec.encode(data)))

    @Test
    fun `round trip without history and without language`() {
        val small = data.copy(history = emptyList(), settings = data.settings.copy(language = null))
        assertEquals(small, BackupCodec.decode(BackupCodec.encode(small)))
    }

    @Test
    fun `newer format is refused`() {
        val bytes = ByteArrayOutputStream().also { DataOutputStream(it).writeInt(BACKUP_FORMAT_VERSION + 1) }.toByteArray()
        assertError(BackupError.NewerFormat) { BackupCodec.decode(bytes) }
    }

    @Test
    fun `truncated content is unreadable`() {
        val bytes = BackupCodec.encode(data)
        assertError(BackupError.Unreadable) { BackupCodec.decode(bytes.copyOf(bytes.size / 2)) }
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
