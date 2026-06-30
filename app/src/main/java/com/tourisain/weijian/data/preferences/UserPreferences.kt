package com.tourisain.weijian.data.preferences

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.tourisain.weijian.presentation.settings.SidebarItem
import com.tourisain.weijian.util.AppLocaleManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Keep
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val CURRENT_USER_ID_KEY = stringPreferencesKey("current_user_id")
    private val PRIVACY_PASSWORD_KEY = stringPreferencesKey("privacy_password")
    private val IS_PRIVACY_MODE_ENABLED_KEY = booleanPreferencesKey("is_privacy_mode_enabled")
    private val PRIVACY_POLICY_ACCEPTED_KEY = booleanPreferencesKey("privacy_policy_accepted")
    private val USER_AGREEMENT_ACCEPTED_KEY = booleanPreferencesKey("user_agreement_accepted")
    private val IS_PREMIUM_ENABLED_KEY = booleanPreferencesKey("is_premium_enabled")
    private val WELCOME_SCREEN_SHOWN_KEY = booleanPreferencesKey("welcome_screen_shown")
    private val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
    private val APP_THEME_KEY = stringPreferencesKey("app_theme")
    private val ENABLE_SMART_REMINDERS_KEY = booleanPreferencesKey("enable_smart_reminders")
    private val ENABLE_SMART_CATEGORIZATION_KEY = booleanPreferencesKey("enable_smart_categorization")
    private val ENABLE_SMART_SUGGESTIONS_KEY = booleanPreferencesKey("enable_smart_suggestions")
    private val USER_INTERFACE_MODE_KEY = stringPreferencesKey("user_interface_mode")
    private val DASHBOARD_LAYOUT_KEY = stringPreferencesKey("dashboard_layout")
    private val TEXT_SCALE_KEY = stringPreferencesKey("text_scale")
    private val TEXT_COLOR_KEY = stringPreferencesKey("text_color")
    private val COLOR_SCHEME_KEY = stringPreferencesKey("color_scheme")
    private val CONFIRM_BEFORE_EXIT_KEY = booleanPreferencesKey("confirm_before_exit")
    private val APP_FONT_KEY = stringPreferencesKey("app_font")
    private val DYNAMIC_THEME_KEY = booleanPreferencesKey("dynamic_theme")
    private val USED_ACTIVATION_CODES_KEY = stringPreferencesKey("used_activation_codes")
    private val ACTIVATION_USERNAME_KEY = stringPreferencesKey("activation_username")
    private val ACTIVATION_EMAIL_KEY = stringPreferencesKey("activation_email")
    private val PENDING_ACTIVATION_CODE_KEY = stringPreferencesKey("pending_activation_code")
    private val MEMBERSHIP_SYSTEM_VERSION_KEY = intPreferencesKey("membership_system_version")
    private val MEMBERSHIP_PROTECTION_VERSION_KEY = intPreferencesKey("membership_protection_version")
    private val ENCRYPTED_MEMBERSHIP_STATE_KEY = stringPreferencesKey("encrypted_membership_state")
    private val WEBDAV_SERVER_URL_KEY = stringPreferencesKey("webdav_server_url")
    private val WEBDAV_USERNAME_KEY = stringPreferencesKey("webdav_username")
    private val WEBDAV_PASSWORD_KEY = stringPreferencesKey("webdav_password")
    private val IS_AUTO_CLOUD_BACKUP_ENABLED_KEY = booleanPreferencesKey("is_auto_cloud_backup_enabled")
    private val IS_DATA_SYNC_ENABLED_KEY = booleanPreferencesKey("is_data_sync_enabled")
    private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
    private val LAST_UPDATE_CHECK_TIME_KEY = longPreferencesKey("last_update_check_time")
    private val DISMISSED_UPDATE_VERSION_CODE_KEY = intPreferencesKey("dismissed_update_version_code")
    private val LAST_SHOWN_RELEASE_NOTES_VERSION_CODE_KEY = intPreferencesKey("last_shown_release_notes_version_code")
    private val POMODORO_FOCUS_MINUTES_KEY = intPreferencesKey("pomodoro_focus_minutes")
    private val POMODORO_BREAK_MINUTES_KEY = intPreferencesKey("pomodoro_break_minutes")
    private val POMODORO_TODAY_DATE_KEY = stringPreferencesKey("pomodoro_today_date")
    private val POMODORO_TODAY_COUNT_KEY = intPreferencesKey("pomodoro_today_count")
    private val POMODORO_TODAY_FOCUS_SECONDS_KEY = intPreferencesKey("pomodoro_today_focus_seconds")
    private val POMODORO_AUTO_SWITCH_KEY = booleanPreferencesKey("pomodoro_auto_switch")
    private val POMODORO_NOTIFY_SOUND_KEY = booleanPreferencesKey("pomodoro_notify_sound")
    private val POMODORO_NOTIFY_VIBRATE_KEY = booleanPreferencesKey("pomodoro_notify_vibrate")
    private val POMODORO_RINGTONE_URI_KEY = stringPreferencesKey("pomodoro_ringtone_uri")
    private val POMODORO_LONG_BREAK_MINUTES_KEY = intPreferencesKey("pomodoro_long_break_minutes")
    private val POMODORO_LONG_BREAK_INTERVAL_KEY = intPreferencesKey("pomodoro_long_break_interval")
    private val POMODORO_ENABLE_LONG_BREAK_KEY = booleanPreferencesKey("pomodoro_enable_long_break")
    private val POMODORO_HISTORY_KEY = stringPreferencesKey("pomodoro_history")
    private val SIDEBAR_ITEMS_KEY = stringPreferencesKey("sidebar_items")

    private val cardKeys = listOf("note", "account")

    val currentUserId: Flow<String?> = context.userDataStore.data
        .map { it[CURRENT_USER_ID_KEY] }

    val privacyPassword: Flow<String?> = context.userDataStore.data
        .map { it[PRIVACY_PASSWORD_KEY] }

    val isPrivacyModeEnabled: Flow<Boolean> = context.userDataStore.data
        .map { it[IS_PRIVACY_MODE_ENABLED_KEY] ?: false }

    val isPrivacyPolicyAccepted: Flow<Boolean> = context.userDataStore.data
        .map { it[PRIVACY_POLICY_ACCEPTED_KEY] ?: false }

    val isUserAgreementAccepted: Flow<Boolean> = context.userDataStore.data
        .map { it[USER_AGREEMENT_ACCEPTED_KEY] ?: false }

    val isPremiumEnabled: Flow<Boolean> = context.userDataStore.data
        .map { it[IS_PREMIUM_ENABLED_KEY] ?: false }

    val isWelcomeScreenShown: Flow<Boolean> = context.userDataStore.data
        .map { it[WELCOME_SCREEN_SHOWN_KEY] ?: false }

    val webDavServerUrl: Flow<String?> = context.userDataStore.data
        .map { it[WEBDAV_SERVER_URL_KEY] }

    val webDavUsername: Flow<String?> = context.userDataStore.data
        .map { it[WEBDAV_USERNAME_KEY] }

    val webDavPassword: Flow<String?> = context.userDataStore.data
        .map { it[WEBDAV_PASSWORD_KEY] }

    val isAutoCloudBackupEnabled: Flow<Boolean> = context.userDataStore.data
        .map { it[IS_AUTO_CLOUD_BACKUP_ENABLED_KEY] ?: false }

    val isDataSyncEnabled: Flow<Boolean> = context.userDataStore.data
        .map { it[IS_DATA_SYNC_ENABLED_KEY] ?: false }

    val lastSyncTime: Flow<Long> = context.userDataStore.data
        .map { it[LAST_SYNC_TIME_KEY] ?: 0 }

    val pomodoroFocusMinutes: Flow<Int> = context.userDataStore.data
        .map { it[POMODORO_FOCUS_MINUTES_KEY] ?: 25 }

    val pomodoroBreakMinutes: Flow<Int> = context.userDataStore.data
        .map { it[POMODORO_BREAK_MINUTES_KEY] ?: 5 }

    val pomodoroTodayDate: Flow<String?> = context.userDataStore.data
        .map { it[POMODORO_TODAY_DATE_KEY] }

    val pomodoroTodayCount: Flow<Int> = context.userDataStore.data
        .map { it[POMODORO_TODAY_COUNT_KEY] ?: 0 }

    val pomodoroTodayFocusSeconds: Flow<Int> = context.userDataStore.data
        .map { it[POMODORO_TODAY_FOCUS_SECONDS_KEY] ?: 0 }

    val pomodoroAutoSwitch: Flow<Boolean> = context.userDataStore.data
        .map { it[POMODORO_AUTO_SWITCH_KEY] ?: false }

    val pomodoroNotifySound: Flow<Boolean> = context.userDataStore.data
        .map { it[POMODORO_NOTIFY_SOUND_KEY] ?: true }

    val pomodoroNotifyVibrate: Flow<Boolean> = context.userDataStore.data
        .map { it[POMODORO_NOTIFY_VIBRATE_KEY] ?: true }

    val pomodoroRingtoneUri: Flow<String?> = context.userDataStore.data
        .map { it[POMODORO_RINGTONE_URI_KEY] }

    val pomodoroLongBreakMinutes: Flow<Int> = context.userDataStore.data
        .map { it[POMODORO_LONG_BREAK_MINUTES_KEY] ?: 15 }

    val pomodoroLongBreakInterval: Flow<Int> = context.userDataStore.data
        .map { it[POMODORO_LONG_BREAK_INTERVAL_KEY] ?: 4 }

    val pomodoroEnableLongBreak: Flow<Boolean> = context.userDataStore.data
        .map { it[POMODORO_ENABLE_LONG_BREAK_KEY] ?: false }

    val pomodoroHistory: Flow<String?> = context.userDataStore.data
        .map { it[POMODORO_HISTORY_KEY] }

    val appLanguage: Flow<String?> = context.userDataStore.data
        .map { it[APP_LANGUAGE_KEY] ?: "system" }

    val appTheme: Flow<String?> = context.userDataStore.data
        .map { it[APP_THEME_KEY] }

    val enableSmartReminders: Flow<Boolean> = context.userDataStore.data
        .map { it[ENABLE_SMART_REMINDERS_KEY] ?: true }

    val enableSmartCategorization: Flow<Boolean> = context.userDataStore.data
        .map { it[ENABLE_SMART_CATEGORIZATION_KEY] ?: true }

    val enableSmartSuggestions: Flow<Boolean> = context.userDataStore.data
        .map { it[ENABLE_SMART_SUGGESTIONS_KEY] ?: true }

    val userInterfaceMode: Flow<String?> = context.userDataStore.data
        .map { it[USER_INTERFACE_MODE_KEY] }

    val dashboardLayout: Flow<String?> = context.userDataStore.data
        .map { it[DASHBOARD_LAYOUT_KEY] }

    val textScale: Flow<String?> = context.userDataStore.data
        .map { it[TEXT_SCALE_KEY] }

    val textColor: Flow<String?> = context.userDataStore.data
        .map { it[TEXT_COLOR_KEY] }

    val colorScheme: Flow<String?> = context.userDataStore.data
        .map { it[COLOR_SCHEME_KEY] }

    val confirmBeforeExit: Flow<Boolean> = context.userDataStore.data
        .map { it[CONFIRM_BEFORE_EXIT_KEY] ?: true }

    val appFont: Flow<String?> = context.userDataStore.data
        .map { it[APP_FONT_KEY] }

    val dynamicTheme: Flow<Boolean?> = context.userDataStore.data
        .map { it[DYNAMIC_THEME_KEY] }

    val activationUsername: Flow<String> = context.userDataStore.data
        .map { it[ACTIVATION_USERNAME_KEY].orEmpty() }

    val activationEmail: Flow<String> = context.userDataStore.data
        .map { it[ACTIVATION_EMAIL_KEY].orEmpty() }

    val pendingActivationCode: Flow<String> = context.userDataStore.data
        .map { it[PENDING_ACTIVATION_CODE_KEY].orEmpty() }

    val membershipSystemVersion: Flow<Int> = context.userDataStore.data
        .map { it[MEMBERSHIP_SYSTEM_VERSION_KEY] ?: 0 }

    val membershipProtectionVersion: Flow<Int> = context.userDataStore.data
        .map { it[MEMBERSHIP_PROTECTION_VERSION_KEY] ?: 0 }

    suspend fun saveCardVisibility(key: String, visible: Boolean) {
        val cardKey = booleanPreferencesKey("card_visibility_$key")
        context.userDataStore.edit {
            it[cardKey] = visible
        }
    }

    fun getCardVisibilityFlow(): Flow<Map<String, Boolean>> {
        return context.userDataStore.data.map { preferences ->
            val visibilityMap = mutableMapOf<String, Boolean>()
            cardKeys.forEach { key ->
                val cardKey = booleanPreferencesKey("card_visibility_$key")
                preferences[cardKey]?.let { visible ->
                    visibilityMap[key] = visible
                }
            }
            visibilityMap
        }
    }

    suspend fun getCardVisibility(): Map<String, Boolean> {
        val preferences = context.userDataStore.data.first()
        val visibilityMap = mutableMapOf<String, Boolean>()
        cardKeys.forEach { key ->
            val cardKey = booleanPreferencesKey("card_visibility_$key")
            preferences[cardKey]?.let { visible ->
                visibilityMap[key] = visible
            }
        }
        return visibilityMap
    }

    suspend fun setCurrentUserId(userId: String) {
        context.userDataStore.edit { it[CURRENT_USER_ID_KEY] = userId }
    }

    suspend fun setActivationIdentity(username: String, email: String) {
        context.userDataStore.edit {
            it[ACTIVATION_USERNAME_KEY] = username.trim().take(64)
            it[ACTIVATION_EMAIL_KEY] = email.trim().lowercase().take(128)
        }
    }

    suspend fun setPendingActivationCode(code: String) {
        context.userDataStore.edit {
            it[PENDING_ACTIVATION_CODE_KEY] = code.trim()
        }
    }

    suspend fun clearPendingActivationCode() {
        context.userDataStore.edit { it.remove(PENDING_ACTIVATION_CODE_KEY) }
    }

    suspend fun setAppLanguage(language: String) {
        AppLocaleManager.persistLanguage(context, language)
        context.userDataStore.edit { it[APP_LANGUAGE_KEY] = language }
    }

    suspend fun setWebDavConfig(url: String, username: String, pass: String) {
        context.userDataStore.edit {
            it[WEBDAV_SERVER_URL_KEY] = url
            it[WEBDAV_USERNAME_KEY] = username
            it[WEBDAV_PASSWORD_KEY] = pass
        }
    }

    suspend fun setAutoCloudBackupEnabled(enabled: Boolean) {
        context.userDataStore.edit { it[IS_AUTO_CLOUD_BACKUP_ENABLED_KEY] = enabled }
    }

    suspend fun setDataSyncEnabled(enabled: Boolean) {
        context.userDataStore.edit { it[IS_DATA_SYNC_ENABLED_KEY] = enabled }
    }

    suspend fun setLastSyncTime(time: Long) {
        context.userDataStore.edit { it[LAST_SYNC_TIME_KEY] = time }
    }

    suspend fun getLastSyncTime(): Long {
        return context.userDataStore.data.first()[LAST_SYNC_TIME_KEY] ?: 0
    }

    suspend fun isDataSyncEnabled(): Boolean {
        return context.userDataStore.data.first()[IS_DATA_SYNC_ENABLED_KEY] ?: false
    }

    suspend fun getLastUpdateCheckTime(): Long {
        return context.userDataStore.data.first()[LAST_UPDATE_CHECK_TIME_KEY] ?: 0L
    }

    suspend fun setLastUpdateCheckTime(time: Long) {
        context.userDataStore.edit { it[LAST_UPDATE_CHECK_TIME_KEY] = time }
    }

    suspend fun getDismissedUpdateVersionCode(): Int {
        return context.userDataStore.data.first()[DISMISSED_UPDATE_VERSION_CODE_KEY] ?: 0
    }

    suspend fun setDismissedUpdateVersionCode(versionCode: Int) {
        context.userDataStore.edit { it[DISMISSED_UPDATE_VERSION_CODE_KEY] = versionCode }
    }

    suspend fun getLastShownReleaseNotesVersionCode(): Int {
        return context.userDataStore.data.first()[LAST_SHOWN_RELEASE_NOTES_VERSION_CODE_KEY] ?: 0
    }

    suspend fun setLastShownReleaseNotesVersionCode(versionCode: Int) {
        context.userDataStore.edit { it[LAST_SHOWN_RELEASE_NOTES_VERSION_CODE_KEY] = versionCode }
    }

    suspend fun setPomodoroFocusMinutes(minutes: Int) {
        context.userDataStore.edit { it[POMODORO_FOCUS_MINUTES_KEY] = minutes }
    }

    suspend fun setPomodoroBreakMinutes(minutes: Int) {
        context.userDataStore.edit { it[POMODORO_BREAK_MINUTES_KEY] = minutes }
    }

    suspend fun setPomodoroToday(date: String, count: Int, focusSeconds: Int) {
        context.userDataStore.edit {
            it[POMODORO_TODAY_DATE_KEY] = date
            it[POMODORO_TODAY_COUNT_KEY] = count
            it[POMODORO_TODAY_FOCUS_SECONDS_KEY] = focusSeconds
        }
    }

    suspend fun setPomodoroTodayCount(count: Int) {
        context.userDataStore.edit { it[POMODORO_TODAY_COUNT_KEY] = count }
    }

    suspend fun setPomodoroTodayFocusSeconds(seconds: Int) {
        context.userDataStore.edit { it[POMODORO_TODAY_FOCUS_SECONDS_KEY] = seconds }
    }

    suspend fun setPomodoroAutoSwitch(enabled: Boolean) {
        context.userDataStore.edit { it[POMODORO_AUTO_SWITCH_KEY] = enabled }
    }

    suspend fun setPomodoroNotifySound(enabled: Boolean) {
        context.userDataStore.edit { it[POMODORO_NOTIFY_SOUND_KEY] = enabled }
    }

    suspend fun setPomodoroNotifyVibrate(enabled: Boolean) {
        context.userDataStore.edit { it[POMODORO_NOTIFY_VIBRATE_KEY] = enabled }
    }

    suspend fun setPomodoroRingtoneUri(uri: String?) {
        context.userDataStore.edit {
            if (uri == null) {
                it.remove(POMODORO_RINGTONE_URI_KEY)
            } else {
                it[POMODORO_RINGTONE_URI_KEY] = uri
            }
        }
    }

    suspend fun setPomodoroLongBreakMinutes(minutes: Int) {
        context.userDataStore.edit { it[POMODORO_LONG_BREAK_MINUTES_KEY] = minutes }
    }

    suspend fun setPomodoroLongBreakInterval(interval: Int) {
        context.userDataStore.edit { it[POMODORO_LONG_BREAK_INTERVAL_KEY] = interval }
    }

    suspend fun setPomodoroEnableLongBreak(enabled: Boolean) {
        context.userDataStore.edit { it[POMODORO_ENABLE_LONG_BREAK_KEY] = enabled }
    }

    suspend fun setPomodoroHistory(value: String) {
        context.userDataStore.edit { it[POMODORO_HISTORY_KEY] = value }
    }

    suspend fun setPrivacyPassword(password: String) {
        context.userDataStore.edit {
            it[PRIVACY_PASSWORD_KEY] = password
            it[IS_PRIVACY_MODE_ENABLED_KEY] = true
        }
    }

    suspend fun disablePrivacyMode() {
        context.userDataStore.edit { it[IS_PRIVACY_MODE_ENABLED_KEY] = false }
    }

    suspend fun clearMembershipActivationState() {
        context.userDataStore.edit {
            it[IS_PREMIUM_ENABLED_KEY] = false
            it.remove(USED_ACTIVATION_CODES_KEY)
            it.remove(ENCRYPTED_MEMBERSHIP_STATE_KEY)
        }
    }

    suspend fun setMembershipSystemVersion(version: Int) {
        context.userDataStore.edit { it[MEMBERSHIP_SYSTEM_VERSION_KEY] = version }
    }

    suspend fun setMembershipProtectionVersion(version: Int) {
        context.userDataStore.edit { it[MEMBERSHIP_PROTECTION_VERSION_KEY] = version }
    }

    suspend fun setEncryptedMembershipState(value: String) {
        context.userDataStore.edit { it[ENCRYPTED_MEMBERSHIP_STATE_KEY] = value }
    }

    suspend fun getEncryptedMembershipState(): String? {
        return context.userDataStore.data.first()[ENCRYPTED_MEMBERSHIP_STATE_KEY]
    }

    suspend fun clearEncryptedMembershipState() {
        context.userDataStore.edit { it.remove(ENCRYPTED_MEMBERSHIP_STATE_KEY) }
    }

    suspend fun setPrivacyPolicyAccepted(accepted: Boolean) {
        context.userDataStore.edit { it[PRIVACY_POLICY_ACCEPTED_KEY] = accepted }
    }

    suspend fun setUserAgreementAccepted(accepted: Boolean) {
        context.userDataStore.edit { it[USER_AGREEMENT_ACCEPTED_KEY] = accepted }
    }

    suspend fun setWelcomeScreenShown(shown: Boolean) {
        context.userDataStore.edit { it[WELCOME_SCREEN_SHOWN_KEY] = shown }
    }

    suspend fun clearCurrentUserId() {
        context.userDataStore.edit { it.remove(CURRENT_USER_ID_KEY) }
    }

    suspend fun setAppTheme(theme: String) {
        context.userDataStore.edit { preferences ->
            val safeTheme = when (theme) {
                "system", "light", "dark" -> theme
                else -> "system"
            }
            if (preferences[APP_THEME_KEY] != safeTheme) {
                preferences[APP_THEME_KEY] = safeTheme
            }
        }
    }

    suspend fun setEnableSmartReminders(enabled: Boolean) {
        context.userDataStore.edit { it[ENABLE_SMART_REMINDERS_KEY] = enabled }
    }

    suspend fun setEnableSmartCategorization(enabled: Boolean) {
        context.userDataStore.edit { it[ENABLE_SMART_CATEGORIZATION_KEY] = enabled }
    }

    suspend fun setEnableSmartSuggestions(enabled: Boolean) {
        context.userDataStore.edit { it[ENABLE_SMART_SUGGESTIONS_KEY] = enabled }
    }

    suspend fun setUserInterfaceMode(mode: String) {
        context.userDataStore.edit { it[USER_INTERFACE_MODE_KEY] = mode }
    }

    suspend fun setDashboardLayout(layout: String) {
        context.userDataStore.edit { it[DASHBOARD_LAYOUT_KEY] = layout }
    }

    suspend fun setTextScale(scale: String) {
        context.userDataStore.edit { preferences ->
            val safeScale = when (scale) {
                "compact", "normal", "large" -> scale
                else -> "normal"
            }
            if (preferences[TEXT_SCALE_KEY] != safeScale) {
                preferences[TEXT_SCALE_KEY] = safeScale
            }
        }
    }

    suspend fun setTextColor(color: String) {
        context.userDataStore.edit { it[TEXT_COLOR_KEY] = color }
    }

    suspend fun setColorScheme(scheme: String) {
        context.userDataStore.edit { preferences ->
            val safeScheme = when (scheme) {
                "classic", "ios", "paper", "sage", "graphite" -> scheme
                else -> "classic"
            }
            if (preferences[COLOR_SCHEME_KEY] != safeScheme) {
                preferences[COLOR_SCHEME_KEY] = safeScheme
            }
        }
    }

    suspend fun setConfirmBeforeExit(confirm: Boolean) {
        context.userDataStore.edit { it[CONFIRM_BEFORE_EXIT_KEY] = confirm }
    }

    suspend fun setAppFont(font: String) {
        context.userDataStore.edit { preferences ->
            val safeFont = when (font) {
                "system", "serif", "mono" -> font
                else -> "system"
            }
            if (preferences[APP_FONT_KEY] != safeFont) {
                preferences[APP_FONT_KEY] = safeFont
            }
        }
    }

    suspend fun setDynamicTheme(dynamic: Boolean) {
        context.userDataStore.edit { preferences ->
            if (preferences[DYNAMIC_THEME_KEY] != dynamic) {
                preferences[DYNAMIC_THEME_KEY] = dynamic
            }
        }
    }

    suspend fun saveSidebarItems(items: List<SidebarItem>) {
        val json = Json.encodeToString(items)
        context.userDataStore.edit { it[SIDEBAR_ITEMS_KEY] = json }
    }

    fun getSidebarItemsFlow(): Flow<List<SidebarItem>> {
        return context.userDataStore.data.map {
            val json = it[SIDEBAR_ITEMS_KEY]
            json?.let {
                try {
                    Json.decodeFromString<List<SidebarItem>>(it)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    }

    suspend fun getSidebarItems(): List<SidebarItem> {
        val json = context.userDataStore.data.first()[SIDEBAR_ITEMS_KEY]
        return json?.let {
            try {
                Json.decodeFromString<List<SidebarItem>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    suspend fun addUsedActivationCode(code: String) {
        val usedCodes = getUsedActivationCodes()
        if (!usedCodes.contains(code)) {
            usedCodes.add(code)
            val json = Json.encodeToString(usedCodes.distinct().takeLast(200))
            context.userDataStore.edit { it[USED_ACTIVATION_CODES_KEY] = json }
        }
    }

    suspend fun getUsedActivationCodes(): MutableList<String> {
        val json = context.userDataStore.data.first()[USED_ACTIVATION_CODES_KEY]
        return json?.let {
            try {
                Json.decodeFromString<MutableList<String>>(it)
            } catch (e: Exception) {
                mutableListOf()
            }
        } ?: mutableListOf()
    }

    suspend fun isActivationCodeUsed(code: String): Boolean {
        return getUsedActivationCodes().contains(code)
    }
}
