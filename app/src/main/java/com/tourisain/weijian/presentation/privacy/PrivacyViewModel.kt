package com.tourisain.weijian.presentation.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _inputPassword = MutableStateFlow("")
    val inputPassword = _inputPassword.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()
    private val _isPrivacyEnabled = MutableStateFlow(false)
    val isPrivacyEnabled = _isPrivacyEnabled.asStateFlow()
    private val _passwordLength = MutableStateFlow(MAX_PASSWORD_LENGTH)
    val passwordLength = _passwordLength.asStateFlow()
    private var forcePasswordUnlock = false

    init {
        viewModelScope.launch {
            userPreferences.isPrivacyModeEnabled.collect { enabled ->
                _isPrivacyEnabled.value = enabled
                if (!enabled && !forcePasswordUnlock) _isUnlocked.value = true
            }
        }
        viewModelScope.launch {
            userPreferences.privacyPassword.collect { password ->
                _passwordLength.value = expectedPasswordLength(password.orEmpty())
            }
        }
    }

    fun onPasswordChange(newPassword: String) {
        val sanitized = newPassword.filter { it.isDigit() }.take(MAX_PASSWORD_LENGTH)
        if (sanitized.length <= MAX_PASSWORD_LENGTH) {
            _inputPassword.value = sanitized
            _error.value = null
            if (sanitized.length == _passwordLength.value) verifyPassword(sanitized)
        }
    }

    fun startRequiredUnlock() {
        forcePasswordUnlock = true
        _isUnlocked.value = false
        _inputPassword.value = ""
        _error.value = null
    }

    private fun verifyPassword(password: String) {
        viewModelScope.launch {
            val storedPassword = userPreferences.privacyPassword.first().orEmpty()
            val expectedLength = expectedPasswordLength(storedPassword)
            _passwordLength.value = expectedLength
            if (password.length < expectedLength) return@launch
            if (storedPassword == password || (storedPassword.isBlank() && !forcePasswordUnlock)) {
                forcePasswordUnlock = false
                _isUnlocked.value = true
                _error.value = null
            } else {
                _error.value = "wrong_password"
                _inputPassword.value = ""
            }
        }
    }

    fun setPrivacyPassword(password: String) {
        val cleanPassword = password.filter { it.isDigit() }.take(MAX_PASSWORD_LENGTH)
        if (cleanPassword.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) return
        viewModelScope.launch { userPreferences.setPrivacyPassword(cleanPassword) }
    }
    fun disablePrivacyMode(@Suppress("UNUSED_PARAMETER") password: String) { disablePrivacyMode() }
    fun disablePrivacyMode() { viewModelScope.launch { userPreferences.disablePrivacyMode() } }

    private fun expectedPasswordLength(password: String): Int {
        if (password.isBlank()) return MAX_PASSWORD_LENGTH
        return password.length.coerceIn(LEGACY_MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH)
    }

    private companion object {
        const val LEGACY_MIN_PASSWORD_LENGTH = 3
        const val MIN_PASSWORD_LENGTH = 4
        const val MAX_PASSWORD_LENGTH = 6
    }
}

