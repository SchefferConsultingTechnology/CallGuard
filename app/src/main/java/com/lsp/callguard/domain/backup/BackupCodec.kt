package com.lsp.callguard.domain.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/** Versioned binary layout of [BackupData] (JVM only, so it is testable without a device). */
object BackupCodec {

    fun encode(data: BackupData): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(data.formatVersion)
            out.writeLong(data.exportedAt)

            out.writeInt(data.whitelist.size)
            data.whitelist.forEach { entry ->
                out.writeUTF(entry.label)
                out.writeUTF(entry.phoneE164)
                out.writeLong(entry.updatedAt)
            }

            out.writeBoolean(data.settings.blockUnknown)
            out.writeBoolean(data.settings.blockPrivateNumbers)
            out.writeNullableUtf(data.settings.language)

            out.writeInt(data.history.size)
            data.history.forEach { log ->
                out.writeUTF(log.id)
                out.writeNullableUtf(log.phone)
                out.writeNullableUtf(log.normalizedPhone)
                out.writeBoolean(log.allowed)
                out.writeUTF(log.reason)
                out.writeLong(log.createdAt)
            }
        }
        return bytes.toByteArray()
    }

    /** @throws BackupException NewerFormat or Unreadable. */
    fun decode(bytes: ByteArray): BackupData {
        try {
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                val version = input.readInt()
                if (version > BACKUP_FORMAT_VERSION) throw BackupException(BackupError.NewerFormat)
                if (version < 1) throw BackupException(BackupError.Unreadable)

                val exportedAt = input.readLong()

                val whitelist = List(input.readCount()) {
                    BackupEntry(
                        label = input.readUTF(),
                        phoneE164 = input.readUTF(),
                        updatedAt = input.readLong()
                    )
                }

                val settings = BackupSettings(
                    blockUnknown = input.readBoolean(),
                    blockPrivateNumbers = input.readBoolean(),
                    language = input.readNullableUtf()
                )

                val history = List(input.readCount()) {
                    BackupLog(
                        id = input.readUTF(),
                        phone = input.readNullableUtf(),
                        normalizedPhone = input.readNullableUtf(),
                        allowed = input.readBoolean(),
                        reason = input.readUTF(),
                        createdAt = input.readLong()
                    )
                }

                return BackupData(version, exportedAt, whitelist, settings, history)
            }
        } catch (e: IOException) {
            throw BackupException(BackupError.Unreadable)
        }
    }

    private fun DataOutputStream.writeNullableUtf(value: String?) {
        writeBoolean(value != null)
        if (value != null) writeUTF(value)
    }

    private fun DataInputStream.readNullableUtf(): String? = if (readBoolean()) readUTF() else null

    private fun DataInputStream.readCount(): Int {
        val count = readInt()
        if (count < 0 || count > MAX_ITEMS) throw BackupException(BackupError.Unreadable)
        return count
    }

    private const val MAX_ITEMS = 1_000_000
}
