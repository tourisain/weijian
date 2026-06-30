package com.tourisain.weijian.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.util.ErrorReporter
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsPreferencesViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val appTheme = userPreferences.appTheme.stablePreference("system")
    val dynamicTheme = userPreferences.dynamicTheme.stablePreference(true)
    val appFont = userPreferences.appFont.stablePreference("system")
    val textScale = userPreferences.textScale.stablePreference("normal")
    val colorScheme = userPreferences.colorScheme.stablePreference("classic")
    val confirmBeforeExit = userPreferences.confirmBeforeExit.stablePreference(true)
    val smartReminders = userPreferences.enableSmartReminders.stablePreference(true)
    val smartCategorization = userPreferences.enableSmartCategorization.stablePreference(true)
    val smartSuggestions = userPreferences.enableSmartSuggestions.stablePreference(true)
    val privacyModeEnabled = userPreferences.isPrivacyModeEnabled.stablePreference(false)
    val privacyPassword = userPreferences.privacyPassword.stablePreference(null)

    fun setAppTheme(value: String) = launch { userPreferences.setAppTheme(value) }
    fun setDynamicTheme(value: Boolean) = launch { userPreferences.setDynamicTheme(value) }
    fun setAppFont(value: String) = launch { userPreferences.setAppFont(value) }
    fun setTextScale(value: String) = launch { userPreferences.setTextScale(value) }
    fun setColorScheme(value: String) = launch { userPreferences.setColorScheme(value) }
    fun setConfirmBeforeExit(value: Boolean) = launch { userPreferences.setConfirmBeforeExit(value) }
    fun setSmartReminders(value: Boolean) = launch { userPreferences.setEnableSmartReminders(value) }
    fun setSmartCategorization(value: Boolean) = launch { userPreferences.setEnableSmartCategorization(value) }
    fun setSmartSuggestions(value: Boolean) = launch { userPreferences.setEnableSmartSuggestions(value) }
    fun setPrivacyPassword(value: String) = launch { userPreferences.setPrivacyPassword(value) }
    fun disablePrivacyMode() = launch { userPreferences.disablePrivacyMode() }

    private fun <T> Flow<T>.stablePreference(defaultValue: T) = catch { error ->
        ErrorReporter.reportException(context, error)
        emit(defaultValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultValue)

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { ErrorReporter.reportException(context, it) }
        }
    }
}
