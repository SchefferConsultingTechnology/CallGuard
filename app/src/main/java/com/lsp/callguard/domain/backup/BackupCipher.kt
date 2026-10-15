package com.lsp.callguard.domain.backup

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-protected envelope for exported backups (constitution II: exports are encrypted).
 * Layout: "CGBK" · version · salt(16) · iv(12) · AES-256-GCM ciphertext+tag.
 * GCM authenticates the content, so a wrong password and a tampered file fail the same way.
 */
object BackupCipher {

    private val MAGIC = byteArrayOf('C'.code.toByte(), 'G'.code.toByte(), 'B'.code.toByte(), 'K'.code.toByte())
    private const val ENVELOPE_VERSION: Byte = 1
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val TAG_BITS = 128
    private const val KEY_BITS = 256
    private const val ITERATIONS = 120_000
    private const val HEADER_SIZE = 4 + 1 + SALT_SIZE + IV_SIZE

    private val random = SecureRandom()

    fun encrypt(plain: ByteArray, password: String): ByteArray {
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val iv = ByteArray(IV_SIZE).also(random::nextBytes)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keyFor(password, salt), GCMParameterSpec(TAG_BITS, iv))

        return MAGIC + byteArrayOf(ENVELOPE_VERSION) + salt + iv + cipher.doFinal(plain)
    }

    /** @throws BackupException WrongPasswordOrCorrupted, NewerFormat or Unreadable. */
    fun decrypt(file: ByteArray, password: String): ByteArray {
        if (file.size <= HEADER_SIZE || !file.copyOfRange(0, 4).contentEquals(MAGIC)) {
            throw BackupException(BackupError.Unreadable)
        }
        if (file[4] > ENVELOPE_VERSION) throw BackupException(BackupError.NewerFormat)

        val salt = file.copyOfRange(5, 5 + SALT_SIZE)
        val iv = file.copyOfRange(5 + SALT_SIZE, HEADER_SIZE)

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keyFor(password, salt), GCMParameterSpec(TAG_BITS, iv))
            cipher.doFinal(file, HEADER_SIZE, file.size - HEADER_SIZE)
        } catch (e: GeneralSecurityException) {
            throw BackupException(BackupError.WrongPasswordOrCorrupted)
        }
    }

    private fun keyFor(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
        try {
            val encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return SecretKeySpec(encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
