package com.tourisain.weijian.presentation.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupHealthModelTest {
    @Test
    fun healthWarnsWhenNoLocalOrWebDavBackupExists() {
        val health = backupHealthState(
            localBackupCount = 0,
            latestLocalBackupAt = 0L,
            webDavStatus = WebDavStatusUiState(),
            isAutoBackupEnabled = false
        )

        assertEquals(BackupHealthLevel.Attention, health.level)
    }

    @Test
    fun healthPrefersWebDavWhenLatestCheckSucceededAndAutoBackupIsEnabled() {
        val health = backupHealthState(
            localBackupCount = 2,
            latestLocalBackupAt = 1_000L,
            webDavStatus = WebDavStatusUiState(lastCheckedAt = 2_000L, lastSuccess = true),
            isAutoBackupEnabled = true
        )

        assertEquals(BackupHealthLevel.WebDavReady, health.level)
        assertEquals(2_000L, health.referenceTime)
    }

    @Test
    fun healthUsesLocalBackupWhenWebDavIsOff() {
        val health = backupHealthState(
            localBackupCount = 3,
            latestLocalBackupAt = 1_000L,
            webDavStatus = WebDavStatusUiState(),
            isAutoBackupEnabled = false
        )

        assertEquals(BackupHealthLevel.LocalReady, health.level)
        assertEquals(1_000L, health.referenceTime)
    }
}
