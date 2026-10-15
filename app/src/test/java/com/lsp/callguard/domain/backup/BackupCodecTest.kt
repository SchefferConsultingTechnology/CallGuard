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

    @Test
    fun `round trip keeps each theme choice`() {
        listOf("system", "light", "dark", null).forEach { theme ->
            val withTheme = data.copy(settings = data.settings.copy(themeMode = theme))
            assertEquals(withTheme, BackupCodec.decode(BackupCodec.encode(withTheme)))
        }
    }

    @Test
    fun `version 1 file without theme is still read`() {
        // Layout written by the 005 encoder: no theme field at the end.
        val bytes = ByteArrayOutputStream().also { stream ->
            DataOutputStream(stream).use { out ->
                out.writeInt(1)
                out.writeLong(42)
                out.writeInt(1)
                out.writeUTF("Mãe")
                out.writeUTF("+5511987654321")
                out.writeLong(10)
                out.writeBoolean(true)
                out.writeBoolean(false)
                out.writeBoolean(true)
                out.writeUTF("pt-BR")
                out.writeInt(0)
            }
        }.toByteArray()

        val decoded = BackupCodec.decode(bytes)

        assertEquals(1, decoded.formatVersion)
        assertEquals(listOf(BackupEntry("Mãe", "+5511987654321", 10)), decoded.whitelist)
        assertEquals(BackupSettings(blockUnknown = true, blockPrivateNumbers = false, language = "pt-BR", themeMode = null), decoded.settings)
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
