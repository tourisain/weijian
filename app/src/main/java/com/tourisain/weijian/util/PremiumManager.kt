package com.tourisain.weijian.util

import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PremiumManager(
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository
) {
    companion object {
        const val MEMBERSHIP_LEVEL_FREE = 0
        const val MEMBERSHIP_LEVEL_LIFETIME = 2
        private const val MEMBERSHIP_SYSTEM_VERSION = 4
        private const val MEMBERSHIP_PROTECTION_VERSION = 3
    }

    suspend fun initializePremiumStatus() = withContext(Dispatchers.IO) {
        val userId = userRepository.ensureLocalUserId()
        resetLegacyMembershipIfNeeded()
        if (userRepository.getCurrentUser(userId) == null) {
            userRepository.createDefaultUser(userId)
        }
        if (userPreferences.membershipProtectionVersion.first() < MEMBERSHIP_PROTECTION_VERSION) {
            userRepository.initializeMembershipProtection(userId)
        }
        userRepository.checkAndUpdateMembershipStatus(userId)
    }

    private suspend fun resetLegacyMembershipIfNeeded() {
        val currentVersion = userPreferences.membershipSystemVersion.first()
        if (currentVersion >= MEMBERSHIP_SYSTEM_VERSION) return

        userRepository.clearAllMemberships()
        userPreferences.clearMembershipActivationState()
        userPreferences.setMembershipSystemVersion(MEMBERSHIP_SYSTEM_VERSION)
    }

    suspend fun isPremiumEnabled(): Boolean = withContext(Dispatchers.IO) {
        userRepository.isUserPro(userRepository.currentUserId.first())
    }

    suspend fun getMembershipLevel(): Int = withContext(Dispatchers.IO) {
        val userId = userRepository.currentUserId.first()
        if (userRepository.isUserPro(userId)) {
            MEMBERSHIP_LEVEL_LIFETIME
        } else {
            MEMBERSHIP_LEVEL_FREE
        }
    }

    suspend fun getMembershipName(): String = when (getMembershipLevel()) {
        MEMBERSHIP_LEVEL_LIFETIME -> "Lifetime"
        else -> "Free"
    }

    suspend fun isFeatureAvailable(feature: PremiumFeature): Boolean {
        val level = getMembershipLevel()
        return when (feature) {
            PremiumFeature.DATA_SYNC,
            PremiumFeature.CLOUD_BACKUP,
            PremiumFeature.ADVANCED_ANALYTICS,
            PremiumFeature.CUSTOM_TEMPLATES,
            PremiumFeature.PRIORITY_SUPPORT,
            PremiumFeature.UNLIMITED_STORAGE -> level >= MEMBERSHIP_LEVEL_LIFETIME
        }
    }

}

enum class PremiumFeature {
    DATA_SYNC,
    CLOUD_BACKUP,
    ADVANCED_ANALYTICS,
    CUSTOM_TEMPLATES,
    PRIORITY_SUPPORT,
    UNLIMITED_STORAGE
}
