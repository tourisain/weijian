package com.tourisain.weijian.util

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import com.tourisain.weijian.R
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.data.repository.UserRepository
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ActivationCodeManager(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository,
    private val securityEnvironmentMonitor: SecurityEnvironmentMonitor
) {
    companion object {
        const val ACTIVATION_CODE_LENGTH = 220
        private const val CURRENT_MEMBERSHIP_SYSTEM_VERSION = 4
        private const val PREFIX_V5 = "WJ5"
        private const val LIFETIME_MEMBERSHIP_LEVEL = 2
        private const val DEVICE_CODE_PREFIX = "WJDC"
        private const val DEVICE_TOKEN_LENGTH = 24
        private const val NEVER_EXPIRES = "NEVER"
        private const val V5_NONCE_BYTES = 12
        private const val V5_TAG_BYTES = 16
        private const val V5_MIN_RAW_BYTES = V5_NONCE_BYTES + V5_TAG_BYTES + 24
        private val EMAIL_REGEX = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
        private val NONCE_REGEX = Regex("[A-Z0-9]{6,24}")
        private val DEVICE_TOKEN_REGEX = Regex("[A-F0-9]{$DEVICE_TOKEN_LENGTH}")
        private val BASE32_TOKEN_REGEX = Regex("[A-Z2-7]{32,220}")
        private val WJ5_CODE_PATTERN = Regex("WJ5(?:-[A-Z2-7]{1,5}){8,90}", RegexOption.IGNORE_CASE)
        private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        private val V5_SECRET_PARTS = arrayOf("wj5", ".tourisain", ".local", ".activation", ".secure", ".v2", ".2026")
    }

    fun buildActivationDeviceCode(username: String, email: String): Result<ActivationDeviceCode> {
        val cleanUsername = normalizeUsername(username)
        val cleanEmail = normalizeEmail(email)
        if (cleanUsername.isBlank()) {
            return Result.failure(IllegalArgumentException(message(R.string.activation_name_required)))
        }
        if (!EMAIL_REGEX.matches(cleanEmail)) {
            return Result.failure(IllegalArgumentException(message(R.string.activation_email_invalid)))
        }

        val compactCode = activationDeviceToken(cleanUsername, cleanEmail)
        return Result.success(
            ActivationDeviceCode(
                username = cleanUsername,
                email = cleanEmail,
                localDeviceFingerprint = getLocalDeviceFingerprint(),
                compactDeviceCode = compactCode,
                deviceCode = formatDeviceCode(compactCode)
            )
        )
    }

    suspend fun validateActivationCode(
        code: String,
        username: String = "",
        email: String = ""
    ): Result<ActivationResult> = withContext(Dispatchers.IO) {
        val normalized = normalizeCode(code)
        val parsed = parseCode(normalized) ?: return@withContext Result.success(
            ActivationResult(false, 0, null, false, false, message(R.string.activation_code_format_invalid))
        )

        if (userPreferences.isActivationCodeUsed(fingerprint(normalized)) && isCurrentUserMembershipActive()) {
            return@withContext Result.success(
                ActivationResult(false, parsed.level, parsed.expiresAt, parsed.deviceBound, false, message(R.string.activation_code_used))
            )
        }

        val environment = securityEnvironmentMonitor.scanNow()
        if (environment.shouldTerminate) {
            return@withContext Result.success(
                ActivationResult(false, parsed.level, parsed.expiresAt, parsed.deviceBound, false, message(R.string.activation_security_environment_risky))
            )
        }

        val request = buildActivationDeviceCode(username, email).getOrElse {
            return@withContext Result.success(
                ActivationResult(false, parsed.level, parsed.expiresAt, true, false, it.message ?: message(R.string.activation_identity_required))
            )
        }
        if (!constantTimeEquals(parsed.scope, request.compactDeviceCode)) {
            return@withContext Result.success(
                ActivationResult(false, parsed.level, parsed.expiresAt, true, false, message(R.string.activation_code_wrong_device))
            )
        }

        Result.success(
            ActivationResult(
                valid = true,
                level = LIFETIME_MEMBERSHIP_LEVEL,
                expiresAt = null,
                deviceBound = parsed.deviceBound,
                lifetime = true,
                message = message(R.string.activation_lifetime_success)
            )
        )
    }

    suspend fun activateWithCode(
        code: String,
        username: String = "",
        email: String = ""
    ): Result<ActivationResult> {
        val request = buildActivationDeviceCode(username, email).getOrElse { error ->
            return Result.success(
                ActivationResult(false, 0, null, true, false, error.message ?: message(R.string.activation_identity_required))
            )
        }
        return activateWithCode(code, request)
    }

    suspend fun activateWithCode(
        code: String,
        request: ActivationDeviceCode
    ): Result<ActivationResult> = withContext(Dispatchers.IO) {
        val normalized = normalizeCode(code)
        val result = validateActivationCodeForDevice(normalized, request).getOrThrow()
        if (!result.valid) return@withContext Result.success(result)

        val userId = userRepository.currentUserId.first()
        val activationHash = fingerprint(normalized)
        userRepository.applyVerifiedActivationMembership(
            userId = userId,
            level = LIFETIME_MEMBERSHIP_LEVEL,
            expiresAt = null,
            activationProof = MembershipActivationProof(
                source = "activation-v5",
                activationHash = activationHash,
                identityHash = sha256("${request.username}|${request.email}"),
                deviceCodeHash = sha256(request.compactDeviceCode)
            )
        )
        userPreferences.setMembershipSystemVersion(CURRENT_MEMBERSHIP_SYSTEM_VERSION)
        userPreferences.addUsedActivationCode(activationHash)
        Result.success(result)
    }

    private suspend fun validateActivationCodeForDevice(
        normalized: String,
        request: ActivationDeviceCode
    ): Result<ActivationResult> {
        val parsed = parseCode(normalized) ?: return Result.success(
            ActivationResult(false, 0, null, false, false, message(R.string.activation_code_format_invalid))
        )

        if (userPreferences.isActivationCodeUsed(fingerprint(normalized)) && isCurrentUserMembershipActive()) {
            return Result.success(
                ActivationResult(false, parsed.level, parsed.expiresAt, parsed.deviceBound, false, message(R.string.activation_code_used))
            )
        }

        val environment = securityEnvironmentMonitor.scanNow()
        if (environment.shouldTerminate) {
            return Result.success(
                ActivationResult(false, parsed.level, parsed.expiresAt, parsed.deviceBound, false, message(R.string.activation_security_environment_risky))
            )
        }

        if (!constantTimeEquals(parsed.scope, request.compactDeviceCode)) {
            return Result.success(
                ActivationResult(false, parsed.level, parsed.expiresAt, true, false, message(R.string.activation_code_wrong_device))
            )
        }

        return Result.success(
            ActivationResult(
                valid = true,
                level = LIFETIME_MEMBERSHIP_LEVEL,
                expiresAt = null,
                deviceBound = parsed.deviceBound,
                lifetime = true,
                message = message(R.string.activation_lifetime_success)
            )
        )
    }

    fun getLocalDeviceFingerprint(): String = localDeviceFingerprint().take(16)

    private fun normalizeCode(code: String): String {
        val compact = code.trim().replace(Regex("\\s+"), "")
        val candidate = WJ5_CODE_PATTERN.find(compact)?.value ?: compact
        return candidate.uppercase(Locale.US).replace(Regex("[^A-Z0-9-]"), "")
    }

    private fun parseCode(code: String): ParsedActivationCode? {
        if (!code.startsWith(PREFIX_V5)) return null
        val token = code.removePrefix(PREFIX_V5).trim('-').replace("-", "")
        return parseV5Token(token)
    }

    private fun parseV5Token(token: String): ParsedActivationCode? {
        if (!BASE32_TOKEN_REGEX.matches(token)) return null
        val raw = base32Decode(token) ?: return null
        if (raw.size < V5_MIN_RAW_BYTES) return null

        val nonceBytes = raw.copyOfRange(0, V5_NONCE_BYTES)
        val tagStart = raw.size - V5_TAG_BYTES
        val cipherText = raw.copyOfRange(V5_NONCE_BYTES, tagStart)
        val tag = raw.copyOfRange(tagStart, raw.size)
        val expectedTag = hmacSha256Bytes(v5TagKey(), PREFIX_V5.toByteArray(Charsets.UTF_8) + nonceBytes + cipherText)
            .copyOfRange(0, V5_TAG_BYTES)
        if (!constantTimeEquals(expectedTag, tag)) return null

        val plain = String(xorWithKeyStream(cipherText, nonceBytes), Charsets.UTF_8)
        val fields = plain.split("|")
        if (fields.size != 6 || fields[0] != "5") return null
        val level = when (fields[1]) {
            "L2", "LIFE", "LIFETIME" -> LIFETIME_MEMBERSHIP_LEVEL
            else -> return null
        }
        if (fields[2] != NEVER_EXPIRES) return null
        val deviceToken = fields[3]
        val issuedAt = fields[4]
        val payloadNonce = fields[5]
        if (!DEVICE_TOKEN_REGEX.matches(deviceToken)) return null
        if (!issuedAt.matches(Regex("\\d{8}T\\d{6}Z"))) return null
        if (!NONCE_REGEX.matches(payloadNonce)) return null

        return ParsedActivationCode(
            level = level,
            scope = deviceToken,
            expiresAt = null,
            deviceBound = true
        )
    }

    private fun activationDeviceToken(username: String, email: String): String {
        val payload = listOf(
            "weijian.device.v5",
            normalizeUsername(username).lowercase(Locale.ROOT),
            normalizeEmail(email),
            localDeviceFingerprint()
        ).joinToString("|")
        return sha256(payload).take(DEVICE_TOKEN_LENGTH)
    }

    private fun localDeviceFingerprint(): String {
        val androidId = DeviceUtil.getDeviceId(context).ifBlank { "unknown-device" }
        val deviceSeed = listOf(
            androidId,
            context.packageName,
            Build.MANUFACTURER.orEmpty(),
            Build.MODEL.orEmpty()
        ).joinToString("|")
        return sha256(deviceSeed)
    }

    private fun fingerprint(value: String): String {
        return sha256("used|$value")
    }

    private fun normalizeUsername(value: String): String {
        return value.trim().replace(Regex("\\s+"), " ").take(64)
    }

    private fun normalizeEmail(value: String): String {
        return value.trim().lowercase(Locale.US).take(128)
    }

    private fun formatDeviceCode(value: String): String {
        return "$DEVICE_CODE_PREFIX-${value.chunked(4).joinToString("-")}"
    }

    private suspend fun isCurrentUserMembershipActive(): Boolean {
        return runCatching {
            val userId = userRepository.currentUserId.first()
            userRepository.isUserPro(userId)
        }.getOrDefault(false)
    }

    private fun hmacSha256Bytes(secret: ByteArray, value: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret, "HmacSHA256"))
        return mac.doFinal(value)
    }

    private fun v5Secret(): ByteArray {
        return V5_SECRET_PARTS.joinToString("").toByteArray(Charsets.UTF_8)
    }

    private fun v5EncKey(): ByteArray {
        return hmacSha256Bytes(v5Secret(), "enc".toByteArray(Charsets.UTF_8))
    }

    private fun v5TagKey(): ByteArray {
        return hmacSha256Bytes(v5Secret(), "tag".toByteArray(Charsets.UTF_8))
    }

    private fun xorWithKeyStream(input: ByteArray, nonce: ByteArray): ByteArray {
        val output = ByteArray(input.size)
        val key = v5EncKey()
        var offset = 0
        var counter = 0
        while (offset < input.size) {
            val counterBytes = byteArrayOf(
                ((counter ushr 24) and 0xFF).toByte(),
                ((counter ushr 16) and 0xFF).toByte(),
                ((counter ushr 8) and 0xFF).toByte(),
                (counter and 0xFF).toByte()
            )
            val block = hmacSha256Bytes(key, nonce + counterBytes)
            for (index in block.indices) {
                if (offset >= input.size) break
                output[offset] = (input[offset].toInt() xor block[index].toInt()).toByte()
                offset += 1
            }
            counter += 1
        }
        return output
    }

    private fun base32Decode(value: String): ByteArray? {
        val output = ByteArrayOutputStream()
        var buffer = 0
        var bitsLeft = 0
        for (char in value) {
            val digit = BASE32_ALPHABET.indexOf(char)
            if (digit < 0) return null
            buffer = (buffer shl 5) or digit
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                output.write((buffer shr bitsLeft) and 0xFF)
                buffer = buffer and ((1 shl bitsLeft) - 1)
            }
        }
        return output.toByteArray()
    }

    private fun constantTimeEquals(left: String, right: String): Boolean {
        val leftBytes = left.uppercase(Locale.US).toByteArray(Charsets.UTF_8)
        val rightBytes = right.uppercase(Locale.US).toByteArray(Charsets.UTF_8)
        var diff = leftBytes.size xor rightBytes.size
        val max = maxOf(leftBytes.size, rightBytes.size)
        for (index in 0 until max) {
            val leftValue = if (index < leftBytes.size) leftBytes[index].toInt() else 0
            val rightValue = if (index < rightBytes.size) rightBytes[index].toInt() else 0
            diff = diff or (leftValue xor rightValue)
        }
        return diff == 0
    }

    private fun constantTimeEquals(left: ByteArray, right: ByteArray): Boolean {
        var diff = left.size xor right.size
        val max = maxOf(left.size, right.size)
        for (index in 0 until max) {
            val leftValue = if (index < left.size) left[index].toInt() else 0
            val rightValue = if (index < right.size) right[index].toInt() else 0
            diff = diff or (leftValue xor rightValue)
        }
        return diff == 0
    }

    private fun message(@StringRes id: Int): String = context.getString(id)

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02X".format(it) }
    }
}

data class ActivationDeviceCode(
    val username: String,
    val email: String,
    val localDeviceFingerprint: String,
    val compactDeviceCode: String,
    val deviceCode: String
)

data class ActivationResult(
    val valid: Boolean,
    val level: Int,
    val expiresAt: Long?,
    val deviceBound: Boolean,
    val lifetime: Boolean,
    val message: String
)

private data class ParsedActivationCode(
    val level: Int,
    val scope: String,
    val expiresAt: Long?,
    val deviceBound: Boolean
)
