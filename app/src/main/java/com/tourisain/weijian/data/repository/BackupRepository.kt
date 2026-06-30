package com.tourisain.weijian.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.AppDatabase
import com.tourisain.weijian.data.model.BackupData
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.util.CryptoUtil
import com.tourisain.weijian.util.DeviceUtil
import com.tourisain.weijian.util.WebDavClient
import com.tourisain.weijian.util.WebDavRemoteFile
import com.tourisain.weijian.util.ErrorLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

data class BackupRestoreOutcome(
    val sourceName: String? = null,
    val safetyBackupName: String
)

data class BackupPreviewData(
    val sourceName: String,
    val data: BackupData
)

@Singleton
class BackupRepository @Inject constructor(
    private val db: AppDatabase,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder().create()

    suspend fun createBackup(password: String? = null): ByteArray = withContext(Dispatchers.IO) {
        generateBackupBytes(password)
    }

    suspend fun createLocalBackup(password: String? = null): Result<LocalBackupFile> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = generateBackupBytes(password)
            val directory = localBackupDirectory()
            val file = File(directory, "$LOCAL_BACKUP_PREFIX${System.currentTimeMillis()}$LOCAL_BACKUP_EXTENSION")
            file.writeBytes(bytes)
            pruneLocalBackupHistory(directory)
            file.toLocalBackupFile()
        }
    }

    suspend fun listLocalBackups(): Result<List<LocalBackupFile>> = withContext(Dispatchers.IO) {
        runCatching {
            localBackupDirectory()
                .listFiles()
                .orEmpty()
                .filter { it.isFile }
                .map { it.toLocalBackupFile() }
                .let { normalizeLocalBackupFiles(it) }
        }
    }

    suspend fun restoreLocalBackup(fileName: String, password: String? = null): Result<BackupRestoreOutcome> = withContext(Dispatchers.IO) {
        try {
            val file = localBackupFile(fileName)
            if (!file.exists() || !file.isFile) {
                return@withContext Result.failure(Exception(context.getString(R.string.backup_local_file_missing)))
            }
            restoreBackupBytes(file.readBytes(), password)
                .map { it.copy(sourceName = file.name) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLocalBackup(fileName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = localBackupFile(fileName)
            if (!file.exists() || !file.isFile) {
                throw IllegalArgumentException(context.getString(R.string.backup_local_file_missing))
            }
            if (!file.delete()) {
                throw IllegalStateException(context.getString(R.string.backup_local_delete_failed))
            }
        }
    }

    suspend fun verifyBackupBytes(data: ByteArray, password: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            validateBackupJson(decodeBackupBytes(data, password))
        } catch (e: Exception) {
            ErrorLogger.logError(context, "BackupRepository", "Verify backup failed: ${e.message}", e)
            Result.failure(Exception("Backup verification failed: ${e.message}"))
        }
    }

    suspend fun previewLocalBackup(fileName: String, password: String? = null): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            val file = localBackupFile(fileName)
            if (!file.exists() || !file.isFile) {
                return@withContext Result.failure(Exception(context.getString(R.string.backup_local_file_missing)))
            }
            parseBackupDataForPreview(decodeBackupBytes(file.readBytes(), password))
        } catch (e: Exception) {
            ErrorLogger.logError(context, "BackupRepository", "Preview local backup failed: ${e.message}", e)
            Result.failure(Exception("Backup preview failed: ${e.message}"))
        }
    }

    suspend fun createAndUploadCloudBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val isEnabled = userPreferences.isAutoCloudBackupEnabled.first()
            if (!isEnabled) return@withContext Result.failure(Exception("Cloud backup disabled"))
            createAndUploadWebDavBackup(userPreferences.privacyPassword.first()).map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAndUploadWebDavBackup(password: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = userPreferences.webDavServerUrl.first()
            val user = userPreferences.webDavUsername.first()
            val pass = userPreferences.webDavPassword.first()
            if (url.isNullOrBlank()) {
                return@withContext Result.failure(Exception("WebDAV configuration missing"))
            }

            val effectivePassword = password ?: userPreferences.privacyPassword.first()
            val backupBytes = generateBackupBytes(effectivePassword)
            val fileName = "weijian_backup_${System.currentTimeMillis()}.bin"
            WebDavClient(url, user.orEmpty(), pass.orEmpty()).upload(fileName, backupBytes).getOrThrow()
            Result.success(fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreLatestWebDavBackup(password: String? = null): Result<BackupRestoreOutcome> = withContext(Dispatchers.IO) {
        try {
            val url = userPreferences.webDavServerUrl.first()
            val user = userPreferences.webDavUsername.first()
            val pass = userPreferences.webDavPassword.first()
            if (url.isNullOrBlank()) {
                return@withContext Result.failure(Exception("WebDAV configuration missing"))
            }

            val webDav = WebDavClient(url, user.orEmpty(), pass.orEmpty())
            val latestBackup = webDav.listFiles()
                .getOrThrow()
                .filter { it.name.endsWith(".bin", ignoreCase = true) }
                .maxWithOrNull(compareBy<WebDavRemoteFile> { it.lastModified ?: it.name.backupTimestamp() ?: 0L }.thenBy { it.name })
                ?: return@withContext Result.failure(Exception("No WebDAV backup files found"))

            val backupBytes = webDav.download(latestBackup.name).getOrThrow()
            val effectivePassword = password ?: userPreferences.privacyPassword.first()
            val outcome = restoreBackupBytes(backupBytes, effectivePassword).getOrThrow()
            Result.success(outcome.copy(sourceName = latestBackup.name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun previewLatestWebDavBackup(password: String? = null): Result<BackupPreviewData> = withContext(Dispatchers.IO) {
        try {
            val url = userPreferences.webDavServerUrl.first()
            val user = userPreferences.webDavUsername.first()
            val pass = userPreferences.webDavPassword.first()
            if (url.isNullOrBlank()) {
                return@withContext Result.failure(Exception("WebDAV configuration missing"))
            }

            val webDav = WebDavClient(url, user.orEmpty(), pass.orEmpty())
            val latestBackup = webDav.listFiles()
                .getOrThrow()
                .filter { it.name.endsWith(".bin", ignoreCase = true) }
                .maxWithOrNull(compareBy<WebDavRemoteFile> { it.lastModified ?: it.name.backupTimestamp() ?: 0L }.thenBy { it.name })
                ?: return@withContext Result.failure(Exception("No WebDAV backup files found"))

            val backupBytes = webDav.download(latestBackup.name).getOrThrow()
            val effectivePassword = password ?: userPreferences.privacyPassword.first()
            parseBackupDataForPreview(decodeBackupBytes(backupBytes, effectivePassword))
                .map { BackupPreviewData(sourceName = latestBackup.name, data = it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testWebDavConnection(url: String, user: String, pass: String): Result<Unit> {
        return WebDavClient(url, user, pass).checkConnection()
    }

    private suspend fun generateBackupBytes(password: String?): ByteArray {
        try {
            val hasRequiredConsent = userPreferences.isPrivacyPolicyAccepted.first() &&
                userPreferences.isUserAgreementAccepted.first()
            val currentUserId = userPreferences.currentUserId.first()
                ?.takeIf { it.isNotBlank() && it != "unknown" }
                ?: if (hasRequiredConsent) DeviceUtil.getDeviceId(context) else PRE_CONSENT_USER_ID
            val notes = db.noteDao().getAllNotesForSync()
            val noteRevisions = db.noteRevisionDao().getAllRevisionsForSync()
            val accounts = db.accountDao().getAllRecordsForSync()
            val users = db.userDao().getAllUsersSync()
            val categories = db.categoryDao().getAllCategories(currentUserId).first()
            val noteCategories = db.noteCategoryDao().getAllCategories(currentUserId).first()
            
            // 获取软件设置
            val appSettings = mutableMapOf<String, Any>()
            appSettings["appLanguage"] = userPreferences.appLanguage.first() ?: "system"
            appSettings["appTheme"] = userPreferences.appTheme.first() ?: "system"
            appSettings["userInterfaceMode"] = userPreferences.userInterfaceMode.first() ?: "light"
            appSettings["dashboardLayout"] = userPreferences.dashboardLayout.first() ?: "grid"
            appSettings["textScale"] = userPreferences.textScale.first() ?: "normal"
            appSettings["textColor"] = userPreferences.textColor.first() ?: "default"
            appSettings["confirmBeforeExit"] = userPreferences.confirmBeforeExit.first()
            appSettings["appFont"] = userPreferences.appFont.first() ?: "default"
            appSettings["dynamicTheme"] = userPreferences.dynamicTheme.first() ?: false
            appSettings["enableSmartReminders"] = userPreferences.enableSmartReminders.first()
            appSettings["enableSmartCategorization"] = userPreferences.enableSmartCategorization.first()
            appSettings["enableSmartSuggestions"] = userPreferences.enableSmartSuggestions.first()
            
            // 获取卡片可见性
            val cardVisibility = userPreferences.getCardVisibility()
            
            // 获取侧边栏项目
            val backupData = BackupData(
                version = 10,
                timestamp = System.currentTimeMillis(),
                notes = if (notes.isEmpty()) null else notes,
                noteRevisions = if (noteRevisions.isEmpty()) null else noteRevisions,
                accountRecords = if (accounts.isEmpty()) null else accounts,
                user = users.firstOrNull(),
                categories = if (categories.isEmpty()) null else categories,
                noteCategories = if (noteCategories.isEmpty()) null else noteCategories,
                appSettings = appSettings,
                cardVisibility = cardVisibility
            )
            
            val jsonString = gson.toJson(backupData)
            // 1. Compress
            val compressed = CryptoUtil.compress(jsonString)
            // 2. Encrypt if password provided
            return if (!password.isNullOrEmpty()) {
                CryptoUtil.encrypt(compressed, password)
            } else {
                compressed
            }
        } catch (e: Exception) {
            ErrorLogger.logError(context, "BackupRepository", "generateBackupBytes failed: ${e.message}", e)
            throw e
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val path = uri.path
        val lastDotIndex = path?.lastIndexOf('.')
        return if (lastDotIndex != null && lastDotIndex > 0) {
            path.substring(lastDotIndex + 1).lowercase()
        } else {
            ""
        }
    }

    private fun readFileContent(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("无法读取文件")
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun decodeBackupBytes(inputBytes: ByteArray, password: String?): String {
        val decrypted = if (!password.isNullOrEmpty()) {
            CryptoUtil.decrypt(inputBytes, password)
        } else {
            inputBytes
        }

        return try {
            CryptoUtil.decompress(decrypted)
        } catch (e: Exception) {
            String(decrypted, StandardCharsets.UTF_8)
        }
    }

    private fun localBackupDirectory(): File {
        return File(context.filesDir, LOCAL_BACKUP_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private fun localBackupFile(fileName: String): File {
        val safeName = fileName.substringAfterLast('/').substringAfterLast('\\')
        require(safeName == fileName && safeName.startsWith(LOCAL_BACKUP_PREFIX) && safeName.endsWith(LOCAL_BACKUP_EXTENSION)) {
            "Invalid local backup file name"
        }
        return File(localBackupDirectory(), safeName)
    }

    private fun pruneLocalBackupHistory(directory: File) {
        val normalized = directory
            .listFiles()
            .orEmpty()
            .filter { it.isFile }
            .map { it.toLocalBackupFile() }
            .let { normalizeLocalBackupFiles(it, limit = Int.MAX_VALUE) }
        normalized
            .drop(MAX_LOCAL_BACKUP_HISTORY)
            .forEach { backup -> runCatching { File(directory, backup.fileName).delete() } }
    }

    private fun File.toLocalBackupFile(): LocalBackupFile {
        return LocalBackupFile(
            fileName = name,
            sizeBytes = length(),
            lastModified = lastModified()
        )
    }

    suspend fun restoreBackupBytes(data: ByteArray, password: String? = null): Result<BackupRestoreOutcome> = withContext(Dispatchers.IO) {
        try {
            processBackupData(decodeBackupBytes(data, password))
                .map { safetyBackupName -> BackupRestoreOutcome(safetyBackupName = safetyBackupName) }
        } catch (e: Exception) {
            ErrorLogger.logError(context, "BackupRepository", "Restore backup bytes failed: ${e.message}", e)
            Result.failure(Exception("Restore failed: ${e.message}"))
        }
    }

    private fun validateBackupJson(jsonString: String): Result<String> {
        val backupData = parseBackupDataForPreview(jsonString).getOrElse { error ->
            return Result.failure(error)
        }
        val version = backupData.version ?: 0
        val itemCount = backupData.restorableItemCount()

        return Result.success(
            context.getString(
                R.string.backup_verify_summary,
                version,
                itemCount,
                backupData.notes?.size ?: 0,
                backupData.noteRevisions?.size ?: 0,
                backupData.accountRecords?.size ?: 0,
                backupData.noteCategories?.size ?: 0,
                backupData.appSettings?.size ?: 0
            )
        )
    }

    private fun parseBackupDataForPreview(jsonString: String): Result<BackupData> {
        val backupData = try {
            gson.fromJson(jsonString, BackupData::class.java)
        } catch (e: Exception) {
            ErrorLogger.logError(context, "BackupRepository", "Failed to parse backup data: ${e.message}", e)
            null
        } ?: return Result.failure(Exception("Backup data parsing failed"))

        val version = backupData.version ?: 0
        val timestamp = backupData.timestamp ?: 0L
        if (version <= 0 || timestamp <= 0L) {
            return Result.failure(Exception("Backup metadata is invalid"))
        }
        if (backupData.restorableItemCount() == 0) {
            return Result.failure(Exception("Backup file does not contain any restorable data"))
        }
        return Result.success(backupData)
    }

    private fun BackupData.restorableItemCount(): Int {
        return (notes?.size ?: 0) +
            (noteRevisions?.size ?: 0) +
            (accountRecords?.size ?: 0) +
            (categories?.size ?: 0) +
            (noteCategories?.size ?: 0) +
            (appSettings?.size ?: 0) +
            (cardVisibility?.size ?: 0) +
            (if (user != null) 1 else 0)
    }

    suspend fun restoreBackup(uri: Uri, password: String? = null): Result<BackupRestoreOutcome> = withContext(Dispatchers.IO) {
        try {
            if (USE_STABLE_BACKUP_RESTORE) {
                return@withContext restoreBackupFromSingleRead(uri, password)
            }
            val extension = getFileExtension(uri)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("无法读取文件"))
            
            inputStream.use { stream ->
                val fileSize = stream.available()
                if (fileSize > 100 * 1024 * 1024) {
                    return@withContext Result.failure(Exception("备份文件过大，请选择较小的备份文件"))
                }
                
                when (extension) {
                    "json", "txt", "md", "html", "xml", "csv" -> {
                        try {
                            val content = readFileContent(context, uri)
                            return@withContext processBackupData(content)
                                .map { safetyBackupName -> BackupRestoreOutcome(safetyBackupName = safetyBackupName) }
                        } catch (e: Exception) {
                            ErrorLogger.logError(context, "BackupRepository", "Failed to read text backup: ${e.message}", e)
                            return@withContext Result.failure(Exception("读取文本文件失败: ${e.message}"))
                        }
                    }
                    "bin" -> {
                        val inputBytes = stream.readBytes()
                        val jsonString = try {
                            val decrypted = if (!password.isNullOrEmpty()) {
                                try {
                                    CryptoUtil.decrypt(inputBytes, password)
                                } catch (e: Exception) {
                                    ErrorLogger.logError(context, "BackupRepository", "Decryption failed: ${e.message}", e)
                                    return@withContext Result.failure(Exception("解密失败或密码错误"))
                                }
                            } else {
                                inputBytes
                            }
                            
                            try {
                                CryptoUtil.decompress(decrypted)
                            } catch (e: Exception) {
                                ErrorLogger.logError(context, "BackupRepository", "Decompression failed, trying fallback: ${e.message}", e)
                                try {
                                    String(decrypted, StandardCharsets.UTF_8)
                                } catch (e2: Exception) {
                                    ErrorLogger.logError(context, "BackupRepository", "Fallback to string failed: ${e2.message}", e2)
                                    return@withContext Result.failure(Exception("无法解析备份文件"))
                                }
                            }
                        } catch (e: Exception) {
                            ErrorLogger.logError(context, "BackupRepository", "Restore backup failed: ${e.message}", e)
                            return@withContext Result.failure(Exception("解密失败或密码错误"))
                        }
                        
                        return@withContext processBackupData(jsonString)
                            .map { safetyBackupName -> BackupRestoreOutcome(safetyBackupName = safetyBackupName) }
                    }
                    else -> {
                        try {
                            val content = readFileContent(context, uri)
                            return@withContext processBackupData(content)
                                .map { safetyBackupName -> BackupRestoreOutcome(safetyBackupName = safetyBackupName) }
                        } catch (e: Exception) {
                            try {
                                val inputBytes = stream.readBytes()
                                val decrypted = if (!password.isNullOrEmpty()) {
                                    try {
                                        CryptoUtil.decrypt(inputBytes, password)
                                    } catch (e1: Exception) {
                                        inputBytes
                                    }
                                } else {
                                    inputBytes
                                }
                                
                                val jsonStr = try {
                                    CryptoUtil.decompress(decrypted)
                                } catch (e1: Exception) {
                                    String(decrypted, StandardCharsets.UTF_8)
                                }
                                return@withContext processBackupData(jsonStr)
                                    .map { safetyBackupName -> BackupRestoreOutcome(safetyBackupName = safetyBackupName) }
                            } catch (e1: Exception) {
                                ErrorLogger.logError(context, "BackupRepository", "Failed to read unknown format: ${e1.message}", e1)
                                return@withContext Result.failure(Exception("不支持的文件格式: $extension"))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            ErrorLogger.logError(context, "BackupRepository", "Restore backup failed: ${e.message}", e)
            Result.failure(Exception("恢复失败: ${e.message}"))
        }
    }

    private suspend fun restoreBackupFromSingleRead(uri: Uri, password: String?): Result<BackupRestoreOutcome> {
        val extension = getFileExtension(uri)
        val inputBytes = context.contentResolver.openInputStream(uri)
            ?.use { it.readBytes() }
            ?: return Result.failure(Exception("Unable to read backup file"))

        if (inputBytes.size > MAX_BACKUP_FILE_SIZE_BYTES) {
            return Result.failure(Exception("Backup file is too large. Please choose a smaller backup file."))
        }

        return when (extension) {
            "json", "txt", "md", "html", "xml", "csv" -> {
                processBackupData(String(inputBytes, StandardCharsets.UTF_8))
                    .map { safetyBackupName -> BackupRestoreOutcome(safetyBackupName = safetyBackupName) }
            }
            "bin" -> {
                restoreBackupBytes(inputBytes, password)
            }
            else -> {
                val textResult = processBackupData(String(inputBytes, StandardCharsets.UTF_8))
                    .map { safetyBackupName -> BackupRestoreOutcome(safetyBackupName = safetyBackupName) }
                if (textResult.isSuccess) textResult else restoreBackupBytes(inputBytes, password)
            }
        }
    }

    private suspend fun processBackupData(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupData = try {
                gson.fromJson(jsonString, BackupData::class.java)
            } catch (e: Exception) {
                ErrorLogger.logError(context, "BackupRepository", "Failed to parse backup data: ${e.message}", e)
                null
            }
            
            if (backupData == null) {
                return@withContext Result.failure(Exception("备份数据解析失败"))
            }

            val safetyBackupName = createPreRestoreSafetyBackup().getOrElse { error ->
                return@withContext Result.failure(
                    Exception(context.getString(R.string.backup_safety_create_failed, error.message.orEmpty()))
                )
            }
            
            db.withTransaction {
                try {
                    db.userDao().deleteAll()
                    backupData.user?.let { user ->
                        db.userDao().insertUser(
                            user.copy(
                                isPro = false,
                                proExpireDate = null,
                                membershipLevel = 0
                            )
                        )
                    }
                    
                    backupData.notes?.let { db.noteDao().insertNotes(it) }
                    backupData.noteRevisions?.let { db.noteRevisionDao().insertRevisions(it) }
                    backupData.accountRecords?.let { db.accountDao().insertRecords(it) }
                    backupData.categories?.let { list -> list.forEach { db.categoryDao().insertCategory(it) } }
                    backupData.noteCategories?.let { list -> list.forEach { db.noteCategoryDao().insertCategory(it) } }
                    
                    backupData.appSettings?.let { settings ->
                        settings["appLanguage"]?.let { userPreferences.setAppLanguage(it.toString()) }
                        settings["appTheme"]?.let { userPreferences.setAppTheme(it.toString()) }
                        settings["userInterfaceMode"]?.let { userPreferences.setUserInterfaceMode(it.toString()) }
                        settings["dashboardLayout"]?.let { userPreferences.setDashboardLayout(it.toString()) }
                        settings["textScale"]?.let { userPreferences.setTextScale(it.toString()) }
                        settings["textColor"]?.let { userPreferences.setTextColor(it.toString()) }
                        settings["confirmBeforeExit"].asBooleanOrNull()?.let { userPreferences.setConfirmBeforeExit(it) }
                        settings["appFont"]?.let { userPreferences.setAppFont(it.toString()) }
                        settings["dynamicTheme"].asBooleanOrNull()?.let { userPreferences.setDynamicTheme(it) }
                        settings["enableSmartReminders"].asBooleanOrNull()?.let { userPreferences.setEnableSmartReminders(it) }
                        settings["enableSmartCategorization"].asBooleanOrNull()?.let { userPreferences.setEnableSmartCategorization(it) }
                        settings["enableSmartSuggestions"].asBooleanOrNull()?.let { userPreferences.setEnableSmartSuggestions(it) }
                    }
                    
                    backupData.cardVisibility?.let { visibility ->
                        visibility.forEach { (key, value) -> userPreferences.saveCardVisibility(key, value) }
                    }
                    
                    userPreferences.clearMembershipActivationState()
                } catch (e: Exception) {
                    ErrorLogger.logError(context, "BackupRepository", "Transaction failed: ${e.message}", e)
                    throw e
                }
            }
            Result.success(safetyBackupName)
        } catch (e: Exception) {
            ErrorLogger.logError(context, "BackupRepository", "Process backup data failed: ${e.message}", e)
            Result.failure(Exception("Restore failed: ${e.message}"))
        }
    }

    private suspend fun createPreRestoreSafetyBackup(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, PRE_RESTORE_BACKUP_DIR).apply {
                if (!exists()) mkdirs()
            }
            if (!directory.exists() || !directory.isDirectory) {
                throw IllegalStateException("Safety backup directory unavailable")
            }
            val password = userPreferences.privacyPassword.first()
            val bytes = generateBackupBytes(password)
            val fileName = "weijian_pre_restore_${System.currentTimeMillis()}.bin"
            File(directory, fileName).writeBytes(bytes)
            prunePreRestoreBackups(directory)
            fileName
        }
    }

    private fun prunePreRestoreBackups(directory: File) {
        directory.listFiles { file -> file.isFile && file.name.endsWith(".bin", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_PRE_RESTORE_BACKUPS)
            ?.forEach { file -> runCatching { file.delete() } }
    }

    private fun Any?.asBooleanOrNull(): Boolean? = when (this) {
        is Boolean -> this
        is String -> when (lowercase()) {
            "true", "1", "yes", "y" -> true
            "false", "0", "no", "n" -> false
            else -> null
        }
        is Number -> toInt() != 0
        else -> null
    }

    private fun String.backupTimestamp(): Long? {
        return substringAfterLast('_', missingDelimiterValue = "")
            .substringBeforeLast('.', missingDelimiterValue = "")
            .toLongOrNull()
    }
}

private const val PRE_CONSENT_USER_ID = "pre-consent"
private const val MAX_BACKUP_FILE_SIZE_BYTES = 100 * 1024 * 1024
private const val PRE_RESTORE_BACKUP_DIR = "pre_restore_backups"
private const val MAX_PRE_RESTORE_BACKUPS = 5
private val USE_STABLE_BACKUP_RESTORE = true
