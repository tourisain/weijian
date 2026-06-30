package com.tourisain.weijian.presentation.backup

enum class BackupHealthLevel {
    Attention,
    LocalReady,
    WebDavReady
}

data class BackupHealthUiState(
    val level: BackupHealthLevel,
    val referenceTime: Long = 0L
)

fun backupHealthState(
    localBackupCount: Int,
    latestLocalBackupAt: Long,
    webDavStatus: WebDavStatusUiState,
    isAutoBackupEnabled: Boolean
): BackupHealthUiState {
    if (isAutoBackupEnabled && webDavStatus.lastSuccess == true && webDavStatus.lastCheckedAt > 0L) {
        return BackupHealthUiState(
            level = BackupHealthLevel.WebDavReady,
            referenceTime = webDavStatus.lastCheckedAt
        )
    }
    if (localBackupCount > 0 && latestLocalBackupAt > 0L) {
        return BackupHealthUiState(
            level = BackupHealthLevel.LocalReady,
            referenceTime = latestLocalBackupAt
        )
    }
    return BackupHealthUiState(BackupHealthLevel.Attention)
}
