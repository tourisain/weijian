package com.tourisain.weijian.data.repository

data class LocalBackupFile(
    val fileName: String,
    val sizeBytes: Long,
    val lastModified: Long
)

internal fun normalizeLocalBackupFiles(
    files: List<LocalBackupFile>,
    limit: Int = MAX_LOCAL_BACKUP_HISTORY
): List<LocalBackupFile> {
    return files
        .filter { it.fileName.startsWith(LOCAL_BACKUP_PREFIX) && it.fileName.endsWith(LOCAL_BACKUP_EXTENSION) }
        .sortedWith(compareByDescending<LocalBackupFile> { it.lastModified }.thenByDescending { it.fileName })
        .take(limit)
}

internal const val LOCAL_BACKUP_DIR = "backups"
internal const val LOCAL_BACKUP_PREFIX = "weijian_backup_"
internal const val LOCAL_BACKUP_EXTENSION = ".bin"
internal const val MAX_LOCAL_BACKUP_HISTORY = 10
