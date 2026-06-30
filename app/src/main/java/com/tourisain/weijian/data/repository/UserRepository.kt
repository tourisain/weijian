package com.tourisain.weijian.data.repository

import android.content.Context
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tourisain.weijian.data.database.AppDatabase
import com.tourisain.weijian.data.database.dao.UserDao
import com.tourisain.weijian.data.database.entity.UserEntity
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.util.DeviceUtil
import com.tourisain.weijian.util.MembershipActivationProof
import com.tourisain.weijian.util.MembershipStateProtector
import com.tourisain.weijian.util.MembershipValidationResult
import com.tourisain.weijian.util.TimeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class UserRepository @Inject constructor(
    val userDao: UserDao,
    private val db: AppDatabase,
    private val userPreferences: UserPreferences,
    private val membershipStateProtector: MembershipStateProtector,
    @ApplicationContext private val context: Context
) {
    private val membershipStateMutex = Mutex()
    private val identityMigrationMutex = Mutex()
    @Volatile private var legacyUnknownDataMigrated = false

    val currentUserId: Flow<String> = combine(
        userPreferences.currentUserId,
        userPreferences.isPrivacyPolicyAccepted,
        userPreferences.isUserAgreementAccepted
    ) { storedUserId, privacyAccepted, agreementAccepted ->
        if (privacyAccepted && agreementAccepted) {
            normalizeUserId(storedUserId)
        } else {
            PRE_CONSENT_USER_ID
        }
    }

    val currentUserState: Flow<UserEntity?> = currentUserId.flatMapLatest { userId ->
        userDao.getUserByIdFlow(userId)
    }

    suspend fun isUserPro(userId: String): Boolean {
        val user = getValidatedUser(userId)
        val now = TimeManager.getCurrentTime()
        return user?.isPro == true && (user.proExpireDate == null || user.proExpireDate > now)
    }

    suspend fun checkAndUpdateMembershipStatus(userId: String) {
        val user = getValidatedUser(userId) ?: return
        if (!user.isPro) return

        val expireDate = user.proExpireDate
        if (expireDate != null && expireDate < TimeManager.getCurrentTime()) {
            saveMembershipState(user.copy(isPro = false, proExpireDate = null, membershipLevel = 0))
            return
        }

        if (user.membershipLevel != LIFETIME_MEMBERSHIP_LEVEL || user.proExpireDate != null) {
            saveMembershipState(user.copy(isPro = true, proExpireDate = null, membershipLevel = LIFETIME_MEMBERSHIP_LEVEL))
        }
    }

    suspend fun clearAllMemberships() {
        membershipStateMutex.withLock {
            membershipStateProtector.clear()
            userDao.clearAllMemberships()
        }
    }

    suspend fun getCurrentUser(userId: String): UserEntity? {
        return getValidatedUser(userId)
    }

    suspend fun createDefaultUser(userId: String): UserEntity {
        return membershipStateMutex.withLock {
            createDefaultUserLocked(userId)
        }
    }

    suspend fun ensureLocalUserId(): String = withContext(Dispatchers.IO) {
        if (!hasAcceptedRequiredPolicies()) {
            return@withContext PRE_CONSENT_USER_ID
        }
        val userId = localDeviceUserId()
        val storedUserId = userPreferences.currentUserId.first()
        if (storedUserId.isNullOrBlank() || storedUserId == LEGACY_UNKNOWN_USER_ID) {
            userPreferences.setCurrentUserId(userId)
        }
        migrateLegacyUnknownUserData(userId)
        userId
    }

    private suspend fun createDefaultUserLocked(userId: String): UserEntity {
        val user = UserEntity(
            id = userId,
            username = "User",
            passwordHash = "",
            avatarUri = null,
            avatarFrameId = "frame_1",
            isPro = false,
            proExpireDate = null,
            membershipLevel = 0
        )
        membershipStateProtector.saveUserState(user)
        userDao.insertUser(user)
        return user
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun upgradeToPro(userId: String, level: Int = LIFETIME_MEMBERSHIP_LEVEL) {
        var user = getValidatedUser(userId)
        if (user == null) {
            user = createDefaultUser(userId)
        }

        saveMembershipState(
            user.copy(
                isPro = true,
                proExpireDate = null,
                membershipLevel = LIFETIME_MEMBERSHIP_LEVEL
            ),
            MembershipActivationProof(source = "internal")
        )
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun applyMembership(
        userId: String,
        level: Int,
        expiresAt: Long?,
        activationProof: MembershipActivationProof? = null
    ) {
        val user = getValidatedUser(userId) ?: createDefaultUser(userId)
        saveMembershipState(
            user.copy(
                isPro = true,
                proExpireDate = null,
                membershipLevel = LIFETIME_MEMBERSHIP_LEVEL
            ),
            activationProof ?: MembershipActivationProof(source = "internal")
        )
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun applyVerifiedActivationMembership(
        userId: String,
        level: Int,
        expiresAt: Long?,
        activationProof: MembershipActivationProof
    ) {
        val user = userDao.getUserById(userId) ?: createDefaultUser(userId)
        saveMembershipState(
            user.copy(
                isPro = true,
                proExpireDate = null,
                membershipLevel = LIFETIME_MEMBERSHIP_LEVEL
            ),
            activationProof
        )
    }

    suspend fun upgradeMembershipLevel(userId: String, level: Int) {
        upgradeToPro(userId, level)
    }

    suspend fun updateUsername(userId: String, newName: String): UserEntity {
        val cleanName = newName.trim()
        require(cleanName.isNotEmpty()) { "Username cannot be empty" }

        val user = userDao.getUserById(userId) ?: createDefaultUser(userId)
        val updatedUser = user.copy(username = cleanName)
        val changedRows = userDao.updateUsername(user.id, cleanName)
        if (changedRows == 0) {
            userDao.insertUser(updatedUser)
        }
        return updatedUser
    }

    suspend fun updateAvatar(userId: String, avatarUri: String) {
        val user = userDao.getUserById(userId) ?: return
        userDao.updateUser(user.copy(avatarUri = avatarUri))
    }

    suspend fun getMembershipInfo(userId: String): Result<UserEntity> {
        val user = getValidatedUser(userId)
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("User not found"))
        }
    }

    suspend fun initializeMembershipProtection(userId: String) {
        membershipStateMutex.withLock {
            val user = userDao.getUserById(userId) ?: createDefaultUserLocked(userId)
            val protectedUser = if (userPreferences.membershipProtectionVersion.first() < MEMBERSHIP_PROTECTION_VERSION && user.isPro) {
                repairInvalidMembershipLocked(user, "legacy membership protection version")
            } else {
                user
            }
            membershipStateProtector.saveUserState(protectedUser)
            userPreferences.setMembershipProtectionVersion(MEMBERSHIP_PROTECTION_VERSION)
        }
    }

    private suspend fun getValidatedUser(userId: String): UserEntity? {
        return membershipStateMutex.withLock {
            getValidatedUserLocked(userId)
        }
    }

    private suspend fun getValidatedUserLocked(userId: String): UserEntity? {
        val user = userDao.getUserById(userId) ?: return null
        if (userPreferences.membershipProtectionVersion.first() < MEMBERSHIP_PROTECTION_VERSION) {
            return if (user.isPro) {
                repairInvalidMembershipLocked(user, "legacy membership protection version")
            } else {
                membershipStateProtector.saveUserState(user)
                userPreferences.setMembershipProtectionVersion(MEMBERSHIP_PROTECTION_VERSION)
                user
            }
        }
        return when (val result = membershipStateProtector.validate(user)) {
            MembershipValidationResult.Valid -> user
            MembershipValidationResult.Missing -> {
                if (user.isPro) {
                    repairInvalidMembershipLocked(user, "missing encrypted membership state")
                } else {
                    membershipStateProtector.saveUserState(user)
                    user
                }
            }
            is MembershipValidationResult.Invalid -> repairInvalidMembershipLocked(user, result.reason)
        }
    }

    private suspend fun repairInvalidMembershipLocked(user: UserEntity, reason: String): UserEntity {
        val repaired = user.copy(isPro = false, proExpireDate = null, membershipLevel = 0)
        membershipStateProtector.saveUserState(repaired)
        userDao.updateUser(repaired)
        userPreferences.setMembershipProtectionVersion(MEMBERSHIP_PROTECTION_VERSION)
        android.util.Log.w("UserRepository", "Membership state repaired: $reason")
        return repaired
    }

    private suspend fun saveMembershipState(user: UserEntity, activationProof: MembershipActivationProof? = null) {
        membershipStateMutex.withLock {
            membershipStateProtector.saveUserState(user, activationProof)
            userPreferences.setMembershipProtectionVersion(MEMBERSHIP_PROTECTION_VERSION)
            userDao.updateUser(user)
        }
    }

    private fun normalizeUserId(userId: String?): String {
        return userId
            ?.takeIf { it.isNotBlank() && it != LEGACY_UNKNOWN_USER_ID }
            ?: localDeviceUserId()
    }

    private fun localDeviceUserId(): String {
        return DeviceUtil.getDeviceId(context).ifBlank { LOCAL_DEVICE_FALLBACK_USER_ID }
    }

    private suspend fun hasAcceptedRequiredPolicies(): Boolean {
        return userPreferences.isPrivacyPolicyAccepted.first() &&
            userPreferences.isUserAgreementAccepted.first()
    }

    private suspend fun migrateLegacyUnknownUserData(localUserId: String) {
        if (legacyUnknownDataMigrated || localUserId == LEGACY_UNKNOWN_USER_ID) return
        identityMigrationMutex.withLock {
            if (legacyUnknownDataMigrated) return
            membershipStateMutex.withLock {
                db.withTransaction {
                    val writableDb = db.openHelper.writableDatabase
                    mergeLegacyUserRow(writableDb, localUserId)
                    USER_ID_TABLES.forEach { table ->
                        writableDb.execSQL(
                            "UPDATE $table SET user_id = ? WHERE user_id = ?",
                            arrayOf(localUserId, LEGACY_UNKNOWN_USER_ID)
                        )
                    }
                    migrateLegacyNoteCategories(writableDb, localUserId)
                }

                userDao.getUserById(localUserId)?.let { migratedUser ->
                    membershipStateProtector.saveUserState(migratedUser)
                    userPreferences.setMembershipProtectionVersion(MEMBERSHIP_PROTECTION_VERSION)
                }
            }
            legacyUnknownDataMigrated = true
        }
    }

    private fun mergeLegacyUserRow(db: SupportSQLiteDatabase, localUserId: String) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO users (
                id, username, password_hash, is_pro, pro_expire_date, membership_level,
                avatar_uri, avatar_frame_id, created_at
            )
            SELECT ?, username, password_hash, is_pro, pro_expire_date, membership_level,
                avatar_uri, avatar_frame_id, created_at
            FROM users WHERE id = ?
            """.trimIndent(),
            arrayOf(localUserId, LEGACY_UNKNOWN_USER_ID)
        )
        db.execSQL(
            """
            UPDATE users SET
                username = CASE
                    WHEN username = 'User' THEN COALESCE((SELECT username FROM users WHERE id = ? AND username <> ''), username)
                    ELSE username
                END,
                password_hash = CASE
                    WHEN password_hash = '' THEN COALESCE((SELECT password_hash FROM users WHERE id = ?), password_hash)
                    ELSE password_hash
                END,
                is_pro = CASE
                    WHEN is_pro = 1 OR COALESCE((SELECT is_pro FROM users WHERE id = ?), 0) = 1 THEN 1
                    ELSE 0
                END,
                pro_expire_date = CASE
                    WHEN pro_expire_date IS NULL THEN (SELECT pro_expire_date FROM users WHERE id = ?)
                    ELSE pro_expire_date
                END,
                membership_level = MAX(membership_level, COALESCE((SELECT membership_level FROM users WHERE id = ?), 0)),
                avatar_uri = COALESCE(avatar_uri, (SELECT avatar_uri FROM users WHERE id = ?)),
                avatar_frame_id = COALESCE(avatar_frame_id, (SELECT avatar_frame_id FROM users WHERE id = ?)),
                created_at = MIN(created_at, COALESCE((SELECT created_at FROM users WHERE id = ?), created_at))
            WHERE id = ? AND EXISTS (SELECT 1 FROM users WHERE id = ?)
            """.trimIndent(),
            arrayOf(
                LEGACY_UNKNOWN_USER_ID,
                LEGACY_UNKNOWN_USER_ID,
                LEGACY_UNKNOWN_USER_ID,
                LEGACY_UNKNOWN_USER_ID,
                LEGACY_UNKNOWN_USER_ID,
                LEGACY_UNKNOWN_USER_ID,
                LEGACY_UNKNOWN_USER_ID,
                LEGACY_UNKNOWN_USER_ID,
                localUserId,
                LEGACY_UNKNOWN_USER_ID
            )
        )
        db.execSQL(
            "DELETE FROM users WHERE id = ? AND EXISTS (SELECT 1 FROM users WHERE id = ?)",
            arrayOf(LEGACY_UNKNOWN_USER_ID, localUserId)
        )
    }

    private fun migrateLegacyNoteCategories(db: SupportSQLiteDatabase, localUserId: String) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO note_categories (id, user_id, name, color, created_at, sort_order)
            SELECT id, user_id, name, color, created_at,
                COALESCE((SELECT MAX(sort_order) + 1 FROM note_categories WHERE user_id = categories.user_id), 0)
            FROM categories
            WHERE user_id = ? AND type = 'note'
            """.trimIndent(),
            arrayOf(localUserId)
        )
        db.execSQL(
            "DELETE FROM categories WHERE user_id = ? AND type = 'note'",
            arrayOf(localUserId)
        )
    }

    private companion object {
        const val MEMBERSHIP_PROTECTION_VERSION = 3
        const val LIFETIME_MEMBERSHIP_LEVEL = 2
        const val LEGACY_UNKNOWN_USER_ID = "unknown"
        const val LOCAL_DEVICE_FALLBACK_USER_ID = "local-device"
        const val PRE_CONSENT_USER_ID = "pre-consent"
        val USER_ID_TABLES = listOf(
            "notes",
            "account_records",
            "categories",
            "note_categories"
        )
    }
}
