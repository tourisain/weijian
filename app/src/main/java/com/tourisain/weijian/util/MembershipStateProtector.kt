package com.tourisain.weijian.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.tourisain.weijian.data.database.entity.UserEntity
import com.tourisain.weijian.data.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

private const val MEMBERSHIP_LIFETIME_LEVEL = 2
private const val MEMBERSHIP_MIN_PROOF_HASH_LENGTH = 32
private val MEMBERSHIP_TICKET_NONCE_REGEX = Regex("[A-Fa-f0-9]{32,64}")

internal fun isMembershipTicketShapeAllowed(
    isPro: Boolean,
    level: Int,
    expiresAt: Long?,
    proof: MembershipActivationProof
): Boolean {
    if (!isPro) {
        return level == 0 && expiresAt == null && proof.source == "free"
    }
    if (level != MEMBERSHIP_LIFETIME_LEVEL || expiresAt != null) return false
    return when (proof.source) {
        "protected",
        "internal" -> true
        "activation-v5" -> proof.activationHash.length >= MEMBERSHIP_MIN_PROOF_HASH_LENGTH &&
            proof.identityHash.length >= MEMBERSHIP_MIN_PROOF_HASH_LENGTH &&
            proof.deviceCodeHash.length >= MEMBERSHIP_MIN_PROOF_HASH_LENGTH
        else -> false
    }
}

internal fun isMembershipTicketNonceStrong(nonce: String): Boolean {
    return MEMBERSHIP_TICKET_NONCE_REGEX.matches(nonce)
}

@Singleton
class MembershipStateProtector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) {
    suspend fun saveUserState(user: UserEntity, activationProof: MembershipActivationProof? = null) {
        val issuedAt = TimeManager.getCurrentTime()
        val device = deviceBindingHash()
        val appSignature = appSignatureHash()
        val proof = activationProof ?: defaultProofFor(user)
        val nonce = secureTicketNonce()
        val payload = JSONObject()
            .put("version", TICKET_VERSION)
            .put("userId", user.id)
            .put("isPro", user.isPro)
            .put("level", user.membershipLevel)
            .put("expireAt", user.proExpireDate ?: NO_EXPIRE)
            .put("device", device)
            .put("appSignature", appSignature)
            .put("source", proof.source)
            .put("activationHash", proof.activationHash)
            .put("identityHash", proof.identityHash)
            .put("deviceCodeHash", proof.deviceCodeHash)
            .put("issuedAt", issuedAt)
            .put("nonce", nonce)
            .put("ticketDigest", ticketDigest(user, device, appSignature, proof, issuedAt, nonce))
            .toString()
        userPreferences.setEncryptedMembershipState(encrypt(payload))
    }

    suspend fun clear() {
        userPreferences.clearEncryptedMembershipState()
    }

    suspend fun validate(user: UserEntity): MembershipValidationResult {
        val encrypted = userPreferences.getEncryptedMembershipState()
            ?: return MembershipValidationResult.Missing
        val payload = decrypt(encrypted)
            ?: return MembershipValidationResult.Invalid("membership ticket cannot be decrypted")
        val json = runCatching { JSONObject(payload) }.getOrNull()
            ?: return MembershipValidationResult.Invalid("membership ticket is malformed")
        if (json.optInt("version", 0) != TICKET_VERSION) {
            return MembershipValidationResult.Invalid("membership ticket version mismatch")
        }
        if (json.optString("device") != deviceBindingHash()) {
            return MembershipValidationResult.Invalid("membership ticket belongs to another device")
        }
        val appSignature = appSignatureHash()
        if (json.optString("appSignature") != appSignature) {
            return MembershipValidationResult.Invalid("membership ticket app signature mismatch")
        }
        if (json.optString("userId") != user.id) {
            return MembershipValidationResult.Invalid("membership ticket user mismatch")
        }
        if (json.optBoolean("isPro") != user.isPro) {
            return MembershipValidationResult.Invalid("membership state changed outside trusted flow")
        }
        if (json.optInt("level") != user.membershipLevel) {
            return MembershipValidationResult.Invalid("membership level changed outside trusted flow")
        }
        if (json.optLong("expireAt", NO_EXPIRE) != (user.proExpireDate ?: NO_EXPIRE)) {
            return MembershipValidationResult.Invalid("membership expiration changed outside trusted flow")
        }
        val issuedAt = json.optLong("issuedAt", 0L)
        if (issuedAt <= 0L || issuedAt > TimeManager.getCurrentTime() + CLOCK_SKEW_ALLOWANCE_MS) {
            return MembershipValidationResult.Invalid("membership ticket issue time is abnormal")
        }
        val proof = MembershipActivationProof(
            source = json.optString("source"),
            activationHash = json.optString("activationHash"),
            identityHash = json.optString("identityHash"),
            deviceCodeHash = json.optString("deviceCodeHash")
        )
        if (!isMembershipTicketShapeAllowed(user.isPro, user.membershipLevel, user.proExpireDate, proof)) {
            return MembershipValidationResult.Invalid("membership ticket proof is incomplete")
        }
        val nonce = json.optString("nonce")
        if (nonce.isNotBlank() && !isMembershipTicketNonceStrong(nonce)) {
            return MembershipValidationResult.Invalid("membership ticket nonce is weak")
        }
        val expectedDigest = ticketDigest(user, json.optString("device"), appSignature, proof, issuedAt, nonce)
        val legacyDigest = if (nonce.isBlank()) legacyTicketDigest(user, json.optString("device"), appSignature, proof, issuedAt) else null
        val storedDigest = json.optString("ticketDigest")
        if (storedDigest != expectedDigest && storedDigest != legacyDigest) {
            return MembershipValidationResult.Invalid("membership ticket digest mismatch")
        }
        return MembershipValidationResult.Valid
    }

    private fun defaultProofFor(user: UserEntity): MembershipActivationProof {
        return if (user.isPro) {
            MembershipActivationProof(source = SOURCE_PROTECTED)
        } else {
            MembershipActivationProof(source = SOURCE_FREE)
        }
    }

    private fun ticketDigest(
        user: UserEntity,
        device: String,
        appSignature: String,
        proof: MembershipActivationProof,
        issuedAt: Long,
        nonce: String
    ): String {
        val raw = listOf(
            "weijian.membership.ticket.v$TICKET_VERSION",
            user.id,
            user.isPro.toString(),
            user.membershipLevel.toString(),
            (user.proExpireDate ?: NO_EXPIRE).toString(),
            device,
            appSignature,
            proof.source,
            proof.activationHash,
            proof.identityHash,
            proof.deviceCodeHash,
            issuedAt.toString(),
            nonce
        ).joinToString("|")
        return sha256(raw)
    }

    private fun legacyTicketDigest(
        user: UserEntity,
        device: String,
        appSignature: String,
        proof: MembershipActivationProof,
        issuedAt: Long
    ): String {
        val raw = listOf(
            "weijian.membership.ticket.v$TICKET_VERSION",
            user.id,
            user.isPro.toString(),
            user.membershipLevel.toString(),
            (user.proExpireDate ?: NO_EXPIRE).toString(),
            device,
            appSignature,
            proof.source,
            proof.activationHash,
            proof.identityHash,
            proof.deviceCodeHash,
            issuedAt.toString()
        ).joinToString("|")
        return sha256(raw)
    }

    @Suppress("DEPRECATION")
    private fun appSignatureHash(): String {
        val signatures = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo ?: return@runCatching emptyList<ByteArray>()
                val apkSigners = if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
                apkSigners?.map { it.toByteArray() }.orEmpty()
            } else {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                packageInfo.signatures?.map { it.toByteArray() }.orEmpty()
            }
        }.getOrDefault(emptyList())
        val joined = signatures.map { sha256(it) }.sorted().joinToString("|")
        return sha256("${context.packageName}|${joined.ifBlank { "unknown-signature" }}")
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv ?: throw IllegalStateException("Membership encryption IV is missing")
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return "$PREFIX.${Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)}"
    }

    private fun decrypt(value: String): String? {
        return runCatching {
            if (!value.startsWith("$PREFIX.")) return null
            val raw = Base64.decode(value.substringAfter('.'), Base64.NO_WRAP)
            if (raw.size <= GCM_IV_BYTES) return null
            val iv = raw.copyOfRange(0, GCM_IV_BYTES)
            val encrypted = raw.copyOfRange(GCM_IV_BYTES, raw.size)
            val cipher = Cipher.getInstance(AES_GCM)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        }.getOrNull()
    }

    private fun secureTicketNonce(): String {
        val bytes = ByteArray(TICKET_NONCE_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
            generator.init(spec)
            generator.generateKey()
        }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun deviceBindingHash(): String {
        val androidId = runCatching { DeviceUtil.getDeviceId(context) }.getOrDefault("")
        val raw = listOf(
            context.packageName,
            androidId,
            Build.BRAND,
            Build.DEVICE,
            Build.MODEL
        ).joinToString("|")
        return sha256(raw)
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun sha256(value: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val TICKET_VERSION = 3
        const val NO_EXPIRE = -1L
        const val PREFIX = "WJMS3"
        const val KEY_ALIAS = "weijian_membership_state_key_v3"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val AES_GCM = "AES/GCM/NoPadding"
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
        const val CLOCK_SKEW_ALLOWANCE_MS = 5L * 60 * 1000
        const val TICKET_NONCE_BYTES = 16
        const val SOURCE_FREE = "free"
        const val SOURCE_PROTECTED = "protected"
        const val SOURCE_INTERNAL = "internal"
        const val SOURCE_ACTIVATION_V5 = "activation-v5"
    }
}

data class MembershipActivationProof(
    val source: String,
    val activationHash: String = "",
    val identityHash: String = "",
    val deviceCodeHash: String = ""
)

sealed class MembershipValidationResult {
    object Valid : MembershipValidationResult()
    object Missing : MembershipValidationResult()
    data class Invalid(val reason: String) : MembershipValidationResult()
}
