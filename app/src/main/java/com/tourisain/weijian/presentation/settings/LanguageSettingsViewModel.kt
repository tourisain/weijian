package com.tourisain.weijian.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.util.ErrorReporter
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LanguageSettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val currentLanguage = userPreferences.appLanguage
        .catch { error ->
            ErrorReporter.reportException(context, error)
            emit("system")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    fun setLanguage(language: String, onApplied: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                userPreferences.setAppLanguage(language)
                onApplied()
            }.onFailure {
                ErrorReporter.reportException(context, it)
            }
        }
    }
}
