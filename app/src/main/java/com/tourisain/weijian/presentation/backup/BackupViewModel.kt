package com.tourisain.weijian.presentation.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.R
import com.tourisain.weijian.data.repository.LocalBackupFile
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.data.repository.BackupRepository
import com.tourisain.weijian.util.PremiumManager
import com.tourisain.weijian.util.WorkManagerUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val userPreferences: UserPreferences,
    private val premiumManager: PremiumManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState = _uiState.asStateFlow()
    private var latestBackupPassword: String? = null
    private val operationMutex = Mutex()

    private val _backupProgress = MutableStateFlow(0)
    val backupProgress = _backupProgress.asStateFlow()
    private val _webDavStatus = MutableStateFlow(WebDavStatusUiState())
    val webDavStatus = _webDavStatus.asStateFlow()
    private val _localBackups = MutableStateFlow<List<LocalBackupFile>>(emptyList())
    val localBackups = _localBackups.asStateFlow()
    private val _restorePreview = MutableStateFlow<BackupRestorePreview?>(null)
    val restorePreview = _restorePreview.asStateFlow()

    val webDavUrl = MutableStateFlow("")
    val webDavUser = MutableStateFlow("")
    val webDavPass = MutableStateFlow("")
    val isAutoBackupEnabled = MutableStateFlow(false)
    val isPro = MutableStateFlow(false)
    val isDataSyncEnabled = MutableStateFlow(false)
    val lastSyncTime = MutableStateFlow(0L)

    init {
        loadWebDavConfig()
        refreshLocalBackups()
        isDataSyncEnabled.value = false
        lastSyncTime.value = 0L
        viewModelScope.launch { isPro.value = premiumManager.isPremiumEnabled() }
    }

    private fun loadWebDavConfig() {
        viewModelScope.launch {
            webDavUrl.value = userPreferences.webDavServerUrl.first().orEmpty()
            webDavUser.value = userPreferences.webDavUsername.first().orEmpty()
            webDavPass.value = userPreferences.webDavPassword.first().orEmpty()
            isAutoBackupEnabled.value = userPreferences.isAutoCloudBackupEnabled.first()
            _webDavStatus.value = WebDavStatusUiState(
                message = if (webDavUrl.value.isBlank()) {
                    context.getString(R.string.backup_webdav_not_configured)
                } else {
                    ""
                }
            )
        }
    }

    private suspend fun requirePremium(): Boolean {
        val premium = premiumManager.isPremiumEnabled()
        isPro.value = premium
        if (!premium) {
            _uiState.value = BackupUiState.Error(context.getString(R.string.backup_member_required_desc))
        }
        return premium
    }

    private suspend fun runExclusiveOperation(block: suspend () -> Unit) {
        if (!operationMutex.tryLock()) {
            _uiState.value = BackupUiState.Error(context.getString(R.string.backup_operation_in_progress))
            return
        }
        try {
            block()
        } finally {
            operationMutex.unlock()
        }
    }

    fun saveWebDavConfig(url: String, user: String, pass: String, enabled: Boolean) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) {
                    return@runExclusiveOperation
                }

                val normalizedUrl = url.trim()
                val normalizedUser = user.trim()
                val normalizedPass = pass.trim()
                if (enabled && normalizedUrl.isBlank()) {
                    _uiState.value = BackupUiState.Error(context.getString(R.string.backup_webdav_url_required))
                    _webDavStatus.value = WebDavStatusUiState(
                        lastCheckedAt = System.currentTimeMillis(),
                        lastSuccess = false,
                        message = context.getString(R.string.backup_webdav_url_required)
                    )
                    return@runExclusiveOperation
                }
                if (normalizedUrl.isNotBlank() && !normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
                    _uiState.value = BackupUiState.Error(context.getString(R.string.backup_webdav_url_invalid))
                    _webDavStatus.value = WebDavStatusUiState(
                        lastCheckedAt = System.currentTimeMillis(),
                        lastSuccess = false,
                        message = context.getString(R.string.backup_webdav_url_invalid)
                    )
                    return@runExclusiveOperation
                }

                runCatching {
                    userPreferences.setWebDavConfig(normalizedUrl, normalizedUser, normalizedPass)
                    webDavUrl.value = normalizedUrl
                    webDavUser.value = normalizedUser
                    webDavPass.value = normalizedPass

                    if (enabled) {
                        _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_checking_webdav))
                        _webDavStatus.value = WebDavStatusUiState(
                            isChecking = true,
                            message = context.getString(R.string.backup_checking_webdav)
                        )
                        val result = backupRepository.testWebDavConnection(normalizedUrl, normalizedUser, normalizedPass)
                        val now = System.currentTimeMillis()
                        if (result.isSuccess) {
                            userPreferences.setAutoCloudBackupEnabled(true)
                            isAutoBackupEnabled.value = true
                            WorkManagerUtil.scheduleBackupWork(context)
                            _webDavStatus.value = WebDavStatusUiState(
                                lastCheckedAt = now,
                                lastSuccess = true,
                                message = context.getString(R.string.backup_webdav_check_success)
                            )
                            _uiState.value = BackupUiState.Success(context.getString(R.string.backup_webdav_enabled))
                        } else {
                            val errorMessage = result.exceptionOrNull()?.message ?: context.getString(R.string.backup_webdav_failed)
                            userPreferences.setAutoCloudBackupEnabled(false)
                            isAutoBackupEnabled.value = false
                            WorkManagerUtil.cancelBackupWork(context)
                            _webDavStatus.value = WebDavStatusUiState(
                                lastCheckedAt = now,
                                lastSuccess = false,
                                message = errorMessage
                            )
                            _uiState.value = BackupUiState.Error(errorMessage)
                        }
                    } else {
                        userPreferences.setAutoCloudBackupEnabled(false)
                        isAutoBackupEnabled.value = false
                        WorkManagerUtil.cancelBackupWork(context)
                        _webDavStatus.value = WebDavStatusUiState(
                            message = if (normalizedUrl.isBlank()) {
                                context.getString(R.string.backup_webdav_not_configured)
                            } else {
                                context.getString(R.string.backup_webdav_disabled)
                            }
                        )
                        _uiState.value = BackupUiState.Success(context.getString(R.string.backup_webdav_disabled))
                    }
                }.onFailure { e ->
                    val errorMessage = e.message ?: context.getString(R.string.backup_webdav_save_failed)
                    _webDavStatus.value = WebDavStatusUiState(
                        lastCheckedAt = System.currentTimeMillis(),
                        lastSuccess = false,
                        message = errorMessage
                    )
                    _uiState.value = BackupUiState.Error(errorMessage)
                }
            }
        }
    }

    fun checkWebDavConnectionNow() {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                val normalizedUrl = webDavUrl.value.trim()
                val normalizedUser = webDavUser.value.trim()
                val normalizedPass = webDavPass.value.trim()
                if (normalizedUrl.isBlank()) {
                    val message = context.getString(R.string.backup_webdav_url_required)
                    _webDavStatus.value = WebDavStatusUiState(
                        lastCheckedAt = System.currentTimeMillis(),
                        lastSuccess = false,
                        message = message
                    )
                    _uiState.value = BackupUiState.Error(message)
                    return@runExclusiveOperation
                }
                if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
                    val message = context.getString(R.string.backup_webdav_url_invalid)
                    _webDavStatus.value = WebDavStatusUiState(
                        lastCheckedAt = System.currentTimeMillis(),
                        lastSuccess = false,
                        message = message
                    )
                    _uiState.value = BackupUiState.Error(message)
                    return@runExclusiveOperation
                }

                _webDavStatus.value = WebDavStatusUiState(
                    isChecking = true,
                    message = context.getString(R.string.backup_checking_webdav)
                )
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_checking_webdav))
                val result = backupRepository.testWebDavConnection(normalizedUrl, normalizedUser, normalizedPass)
                val now = System.currentTimeMillis()
                if (result.isSuccess) {
                    val message = context.getString(R.string.backup_webdav_check_success)
                    _webDavStatus.value = WebDavStatusUiState(
                        lastCheckedAt = now,
                        lastSuccess = true,
                        message = message
                    )
                    _uiState.value = BackupUiState.Success(message)
                } else {
                    val message = result.exceptionOrNull()?.message ?: context.getString(R.string.backup_webdav_check_failed)
                    _webDavStatus.value = WebDavStatusUiState(
                        lastCheckedAt = now,
                        lastSuccess = false,
                        message = message
                    )
                    _uiState.value = BackupUiState.Error(message)
                }
            }
        }
    }

    fun createBackup(password: String? = null) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_creating))
                _backupProgress.value = 0
                runCatching {
                    for (progress in 10..90 step 20) {
                        _backupProgress.value = progress
                        delay(60)
                    }
                    backupRepository.createBackup(password)
                }.onSuccess { data ->
                    _backupProgress.value = 100
                    latestBackupPassword = password
                    _uiState.value = BackupUiState.BackupReady(data)
                }.onFailure { e ->
                    _backupProgress.value = 0
                    _uiState.value = BackupUiState.Error(e.message ?: context.getString(R.string.backup_create_failed))
                }
            }
        }
    }

    fun createLocalBackup(password: String? = null) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_creating))
                _backupProgress.value = 20
                val result = backupRepository.createLocalBackup(password)
                if (result.isSuccess) {
                    _backupProgress.value = 100
                    refreshLocalBackupsInternal()
                    _uiState.value = BackupUiState.Success(
                        context.getString(R.string.backup_local_created, result.getOrNull()?.fileName.orEmpty())
                    )
                } else {
                    _backupProgress.value = 0
                    _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.backup_create_failed))
                }
            }
        }
    }

    fun uploadBackupToWebDav() {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_webdav_uploading))
                _backupProgress.value = 20
                val result = backupRepository.createAndUploadWebDavBackup(latestBackupPassword)
                if (result.isSuccess) {
                    _backupProgress.value = 100
                    _uiState.value = BackupUiState.Success(
                        context.getString(R.string.backup_webdav_uploaded, result.getOrNull().orEmpty())
                    )
                } else {
                    _backupProgress.value = 0
                    _uiState.value = BackupUiState.Error(
                        result.exceptionOrNull()?.message ?: context.getString(R.string.backup_webdav_upload_failed)
                    )
                }
            }
        }
    }

    fun restoreLatestWebDavBackup(password: String? = null) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_webdav_downloading))
                _backupProgress.value = 20
                val result = backupRepository.restoreLatestWebDavBackup(password)
                if (result.isSuccess) {
                    _backupProgress.value = 100
                    val outcome = result.getOrNull()
                    _uiState.value = BackupUiState.Success(
                        context.getString(
                            R.string.backup_webdav_restored_with_safety,
                            outcome?.sourceName.orEmpty(),
                            outcome?.safetyBackupName.orEmpty()
                        )
                    )
                } else {
                    _backupProgress.value = 0
                    _uiState.value = BackupUiState.Error(
                        result.exceptionOrNull()?.message ?: context.getString(R.string.backup_webdav_restore_failed)
                    )
                }
            }
        }
    }

    fun previewLatestWebDavBackup(password: String? = null) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading("\u6b63\u5728\u9884\u89c8 WebDAV \u5907\u4efd")
                _backupProgress.value = 20
                val result = backupRepository.previewLatestWebDavBackup(password)
                if (result.isSuccess) {
                    val previewData = result.getOrThrow()
                    _restorePreview.value = backupRestorePreview(
                        data = previewData.data,
                        sourceName = previewData.sourceName
                    )
                    _backupProgress.value = 100
                    _uiState.value = BackupUiState.Success("\u5907\u4efd\u9884\u89c8\u5df2\u751f\u6210")
                } else {
                    _backupProgress.value = 0
                    _restorePreview.value = null
                    _uiState.value = BackupUiState.Error(
                        result.exceptionOrNull()?.message ?: "\u5907\u4efd\u9884\u89c8\u5931\u8d25"
                    )
                }
            }
        }
    }

    fun restoreBackup(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_restoring))
                _backupProgress.value = 0
                val result = backupRepository.restoreBackup(uri, password)
                if (result.isSuccess) {
                    _backupProgress.value = 100
                    _uiState.value = BackupUiState.Success(
                        context.getString(
                            R.string.backup_restore_completed_with_safety,
                            result.getOrNull()?.safetyBackupName.orEmpty()
                        )
                    )
                } else {
                    _backupProgress.value = 0
                    _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.backup_restore_failed))
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = BackupUiState.Idle
        _backupProgress.value = 0
    }

    fun refreshLocalBackups() {
        viewModelScope.launch {
            refreshLocalBackupsInternal()
        }
    }

    fun restoreLocalBackup(fileName: String, password: String? = null) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_restoring))
                _backupProgress.value = 20
                val result = backupRepository.restoreLocalBackup(fileName, password)
                if (result.isSuccess) {
                    _backupProgress.value = 100
                    val outcome = result.getOrNull()
                    _uiState.value = BackupUiState.Success(
                        context.getString(
                            R.string.backup_local_restored,
                            outcome?.sourceName.orEmpty(),
                            outcome?.safetyBackupName.orEmpty()
                        )
                    )
                } else {
                    _backupProgress.value = 0
                    _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.backup_local_restore_failed))
                }
            }
        }
    }

    fun previewLocalBackup(fileName: String, password: String? = null) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                _uiState.value = BackupUiState.Loading("\u6b63\u5728\u9884\u89c8\u5907\u4efd")
                val result = backupRepository.previewLocalBackup(fileName, password)
                if (result.isSuccess) {
                    val preview = backupRestorePreview(data = result.getOrThrow(), sourceName = fileName)
                    _restorePreview.value = preview
                    _uiState.value = BackupUiState.Success("\u5907\u4efd\u9884\u89c8\u5df2\u751f\u6210")
                } else {
                    _restorePreview.value = null
                    _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: "\u5907\u4efd\u9884\u89c8\u5931\u8d25")
                }
            }
        }
    }

    fun clearRestorePreview() {
        _restorePreview.value = null
    }

    fun deleteLocalBackup(fileName: String) {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                val result = backupRepository.deleteLocalBackup(fileName)
                if (result.isSuccess) {
                    refreshLocalBackupsInternal()
                    _uiState.value = BackupUiState.Success(context.getString(R.string.backup_local_deleted))
                } else {
                    _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.backup_local_delete_failed))
                }
            }
        }
    }

    fun setLocalBackupSchedule(frequency: String, time: String) {
        runCatching { WorkManagerUtil.scheduleLocalBackupWork(context, frequency, time) }
            .onSuccess { _uiState.value = BackupUiState.Success(context.getString(R.string.backup_local_scheduled)) }
            .onFailure { _uiState.value = BackupUiState.Error(it.message ?: context.getString(R.string.backup_local_schedule_failed)) }
    }

    fun enableLocalAutoBackup(enabled: Boolean) {
        runCatching {
            if (enabled) WorkManagerUtil.scheduleLocalBackupWork(context, "daily", "01:00") else WorkManagerUtil.cancelLocalBackupWork(context)
        }.onSuccess {
            _uiState.value = BackupUiState.Success(
                if (enabled) context.getString(R.string.backup_local_auto_enabled) else context.getString(R.string.backup_local_auto_disabled)
            )
        }.onFailure {
            _uiState.value = BackupUiState.Error(it.message ?: context.getString(R.string.backup_local_auto_failed))
        }
    }

    fun saveCurrentBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                val state = _uiState.value
                if (state !is BackupUiState.BackupReady) {
                    _uiState.value = BackupUiState.Error(context.getString(R.string.backup_create_before_save))
                    return@runExclusiveOperation
                }
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(state.data)
                        output.flush()
                    } ?: throw IllegalStateException(context.getString(R.string.backup_destination_unavailable))
                }.onSuccess {
                    _uiState.value = BackupUiState.Success(context.getString(R.string.backup_saved))
                }.onFailure { e ->
                    _uiState.value = BackupUiState.Error(e.message ?: context.getString(R.string.backup_save_failed))
                }
            }
        }
    }

    fun verifyBackupFile() {
        viewModelScope.launch {
            runExclusiveOperation {
                if (!requirePremium()) return@runExclusiveOperation
                val state = _uiState.value
                if (state !is BackupUiState.BackupReady) {
                    _uiState.value = BackupUiState.Error(context.getString(R.string.backup_create_before_verify))
                    return@runExclusiveOperation
                }
                _uiState.value = BackupUiState.Loading(context.getString(R.string.backup_verifying))
                val result = backupRepository.verifyBackupBytes(state.data, latestBackupPassword)
                if (result.isSuccess) {
                    _backupProgress.value = 100
                    _uiState.value = BackupUiState.Success(result.getOrNull() ?: context.getString(R.string.backup_verified))
                } else {
                    _backupProgress.value = 0
                    _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.backup_verify_failed))
                }
            }
        }
    }

    private suspend fun refreshLocalBackupsInternal() {
        val result = backupRepository.listLocalBackups()
        if (result.isSuccess) {
            _localBackups.value = result.getOrNull().orEmpty()
        } else {
            _localBackups.value = emptyList()
            _uiState.value = BackupUiState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.backup_no_history))
        }
    }
}

sealed class BackupUiState {
    object Idle : BackupUiState()
    data class Loading(val message: String) : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
    data class BackupReady(val data: ByteArray) : BackupUiState()
}

