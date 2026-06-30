package com.tourisain.weijian.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private val MAGIC = byteArrayOf(0x57, 0x4A, 0x45, 0x4E, 0x43, 0x31) // WJENC1
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val KEY_SIZE_BITS = 256
    private const val PBKDF2_ITERATIONS = 120_000
    private const val GCM_TAG_BITS = 128

    fun compress(text: String): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { gzip ->
            gzip.write(text.toByteArray(Charsets.UTF_8))
        }
        return output.toByteArray()
    }

    fun decompress(bytes: ByteArray): String {
        return GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun encrypt(bytes: ByteArray, password: String): ByteArray {
        require(password.isNotBlank()) { "Password cannot be empty" }
        val salt = randomBytes(SALT_SIZE)
        val iv = randomBytes(IV_SIZE)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(GCM_TAG_BITS, iv))
        val encrypted = cipher.doFinal(bytes)
        return MAGIC + salt + iv + encrypted
    }

    fun decrypt(bytes: ByteArray, password: String): ByteArray {
        if (!bytes.startsWith(MAGIC)) return bytes
        require(password.isNotBlank()) { "Password cannot be empty" }

        val offsetSalt = MAGIC.size
        val offsetIv = offsetSalt + SALT_SIZE
        val offsetPayload = offsetIv + IV_SIZE
        require(bytes.size > offsetPayload) { "Invalid encrypted backup" }

        val salt = bytes.copyOfRange(offsetSalt, offsetIv)
        val iv = bytes.copyOfRange(offsetIv, offsetPayload)
        val payload = bytes.copyOfRange(offsetPayload, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(payload)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
        val keyBytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun randomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) return false
        return prefix.indices.all { this[it] == prefix[it] }
    }
}
