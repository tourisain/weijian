package com.tourisain.weijian.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.R
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.data.repository.BackupRepository
import com.tourisain.weijian.data.repository.UserRepository
import com.tourisain.weijian.util.ActivationCodeManager
import com.tourisain.weijian.util.ErrorReporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val backupRepository: BackupRepository,
    private val activationCodeManager: ActivationCodeManager,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val currentUser = userRepository.currentUserState.catch { error ->
        ErrorReporter.reportException(context, error)
        emit(null)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _isActivating = MutableStateFlow(false)
    val isActivating = _isActivating.asStateFlow()

    private val _activationIdentity = MutableStateFlow(
        ActivationIdentityUiState(localFingerprint = activationCodeManager.getLocalDeviceFingerprint())
    )
    val activationIdentity = _activationIdentity.asStateFlow()

    private val _activationFeedback = MutableStateFlow<ActivationFeedbackUiState?>(null)
    val activationFeedback = _activationFeedback.asStateFlow()

    private val _activationCode = MutableStateFlow("")
    val activationCode = _activationCode.asStateFlow()

    val isUserPro = userRepository.currentUserState.mapLatest { user ->
        user?.let { userRepository.isUserPro(it.id) } ?: false
    }.catch { error ->
        ErrorReporter.reportException(context, error)
        emit(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadCurrentUser()
        loadSavedActivationIdentity()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            ensureCurrentUser()
        }
    }

    fun updateActivationUsername(value: String) {
        _activationIdentity.update { it.copy(username = value.take(64)) }
        persistActivationIdentity()
        refreshActivationDevicePreview()
    }

    fun updateActivationEmail(value: String) {
        _activationIdentity.update { it.copy(email = value.take(128)) }
        persistActivationIdentity()
        refreshActivationDevicePreview()
    }

    fun updateActivationCode(value: String) {
        if (_activationCode.value == value) return
        _activationCode.value = value
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.setPendingActivationCode(value)
        }
    }

    fun generateActivationDeviceCode() {
        val state = _activationIdentity.value
        val request = activationCodeManager.buildActivationDeviceCode(state.username, state.email)
        request.onSuccess { activationDevice ->
            _activationIdentity.update {
                it.copy(
                    username = activationDevice.username,
                    email = activationDevice.email,
                    deviceCode = activationDevice.deviceCode,
                    localFingerprint = activationDevice.localDeviceFingerprint
                )
            }
            persistActivationIdentity()
            _toastMessage.value = context.getString(R.string.activation_device_code_generated)
        }.onFailure { error ->
            _activationIdentity.update { it.copy(deviceCode = "") }
            _toastMessage.value = error.message ?: context.getString(R.string.activation_identity_required)
        }
    }

    fun upgradeToPro(activationCode: String, username: String = "", email: String = "") {
        viewModelScope.launch {
            if (_isActivating.value) return@launch

            val cleanCode = activationCode.trim()
            if (cleanCode.isBlank()) {
                showActivationFeedback(context.getString(R.string.activation_code_format_invalid))
                return@launch
            }

            val identity = activationCodeManager.buildActivationDeviceCode(username, email).getOrElse { error ->
                _activationIdentity.update { it.copy(deviceCode = "") }
                showActivationFeedback(error.message ?: context.getString(R.string.activation_identity_required))
                return@launch
            }

            _isActivating.value = true
            showActivationFeedback(context.getString(R.string.activation_activating))
            try {
                _activationIdentity.update {
                    it.copy(
                        username = identity.username,
                        email = identity.email,
                        deviceCode = identity.deviceCode,
                        localFingerprint = identity.localDeviceFingerprint
                    )
                }

                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        withTimeoutOrNull(ACTIVATION_TIMEOUT_MS) {
                            userPreferences.setActivationIdentity(identity.username, identity.email)
                            activationCodeManager
                                .activateWithCode(cleanCode, identity)
                                .getOrThrow()
                        }
                    }
                }.getOrElse { error ->
                    showActivationFeedback(error.message ?: context.getString(R.string.activation_failed))
                    return@launch
                } ?: run {
                    showActivationFeedback(context.getString(R.string.activation_timeout_retry))
                    return@launch
                }

                showActivationFeedback(
                    message = result.message,
                    success = result.valid,
                    level = result.level,
                    expiresAt = result.expiresAt
                )
                if (result.valid) {
                    _activationCode.value = ""
                    withContext(Dispatchers.IO) {
                        userPreferences.clearPendingActivationCode()
                    }
                    loadCurrentUser()
                }
            } finally {
                _isActivating.value = false
            }
        }
    }

    fun getDeviceActivationFingerprint(): String {
        return activationCodeManager.getLocalDeviceFingerprint()
    }

    fun prepareBackup(onReady: (ByteArray) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = backupRepository.createBackup()
                withContext(Dispatchers.Main) { onReady(data) }
            } catch (e: Exception) {
                _toastMessage.value = "Backup failed: ${e.message}"
            }
        }
    }

    fun restoreBackup(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                val result = backupRepository.restoreBackup(uri, password)
                _toastMessage.value = if (result.isSuccess) {
                    loadCurrentUser()
                    "Restore complete"
                } else {
                    "Restore failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                ErrorReporter.reportException(context, e)
                _toastMessage.value = "Restore failed: ${e.message}"
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun updateUsername(newName: String) {
        val cleanName = newName.trim()
        if (cleanName.isEmpty()) {
            _toastMessage.value = "Username cannot be empty"
            return
        }
        viewModelScope.launch {
            try {
                val user = currentUser.value ?: ensureCurrentUser()
                withContext(Dispatchers.IO) {
                    userRepository.updateUsername(user.id, cleanName)
                }
                _toastMessage.value = "Username updated"
                loadCurrentUser()
            } catch (e: Exception) {
                _toastMessage.value = "Update failed: ${e.message}"
            }
        }
    }

    fun updateAvatar(uri: Uri) {
        updateAvatarWithData(uri.toString())
    }

    fun updateAvatarWithData(avatarData: String) {
        viewModelScope.launch {
            runCatching {
                val user = currentUser.value ?: ensureCurrentUser()
                userRepository.updateAvatar(user.id, avatarData)
            }.onSuccess {
                _toastMessage.value = "Avatar updated"
                loadCurrentUser()
            }.onFailure { error ->
                ErrorReporter.reportException(context, error)
                _toastMessage.value = "Avatar update failed: ${error.message}"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun showActivationFeedback(
        message: String,
        success: Boolean = false,
        level: Int = 0,
        expiresAt: Long? = null
    ) {
        _activationFeedback.value = ActivationFeedbackUiState(
            message = message,
            success = success,
            level = level,
            expiresAt = expiresAt
        )
        _toastMessage.value = message
    }

    private fun refreshActivationDevicePreview() {
        val state = _activationIdentity.value
        val request = activationCodeManager.buildActivationDeviceCode(state.username, state.email).getOrNull()
        _activationIdentity.update {
            it.copy(
                deviceCode = request?.deviceCode.orEmpty(),
                localFingerprint = request?.localDeviceFingerprint ?: activationCodeManager.getLocalDeviceFingerprint()
            )
        }
    }

    private fun loadSavedActivationIdentity() {
        viewModelScope.launch {
            runCatching {
                val username = userPreferences.activationUsername.first()
                val email = userPreferences.activationEmail.first()
                val savedActivationCode = userPreferences.pendingActivationCode.first()
                _activationIdentity.update {
                    it.copy(
                        username = username,
                        email = email,
                        localFingerprint = activationCodeManager.getLocalDeviceFingerprint()
                    )
                }
                _activationCode.value = savedActivationCode
                refreshActivationDevicePreview()
            }.onFailure { error ->
                ErrorReporter.reportException(context, error)
            }
        }
    }

    private fun persistActivationIdentity() {
        val state = _activationIdentity.value
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userPreferences.setActivationIdentity(state.username, state.email)
            }.onFailure { error ->
                ErrorReporter.reportException(context, error)
            }
        }
    }

    private suspend fun ensureCurrentUser() =
        withContext(Dispatchers.IO) {
            val userId = userRepository.currentUserId.first()
            userRepository.checkAndUpdateMembershipStatus(userId)
            userRepository.getCurrentUser(userId) ?: userRepository.createDefaultUser(userId)
        }

    private companion object {
        const val ACTIVATION_TIMEOUT_MS = 8_000L
    }
}

data class ActivationIdentityUiState(
    val username: String = "",
    val email: String = "",
    val deviceCode: String = "",
    val localFingerprint: String = ""
)

data class ActivationFeedbackUiState(
    val message: String = "",
    val success: Boolean = false,
    val level: Int = 0,
    val expiresAt: Long? = null
)
