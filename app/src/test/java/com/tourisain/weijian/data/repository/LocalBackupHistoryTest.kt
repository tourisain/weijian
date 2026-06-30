package com.tourisain.weijian.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalBackupHistoryTest {
    @Test
    fun keepsOnlyBackupBinFilesSortedNewestFirst() {
        val files = listOf(
            LocalBackupFile("note.txt", 1, 400),
            LocalBackupFile("weijian_backup_100.bin", 10, 100),
            LocalBackupFile("weijian_backup_300.bin", 30, 300),
            LocalBackupFile("other_backup.bin", 40, 500),
            LocalBackupFile("weijian_backup_200.bin", 20, 200)
        )

        val result = normalizeLocalBackupFiles(files)

        assertEquals(
            listOf("weijian_backup_300.bin", "weijian_backup_200.bin", "weijian_backup_100.bin"),
            result.map { it.fileName }
        )
    }

    @Test
    fun prunesOlderFilesAfterLimit() {
        val files = (1..12).map { index ->
            LocalBackupFile("weijian_backup_$index.bin", index.toLong(), index.toLong())
        }

        val result = normalizeLocalBackupFiles(files, limit = 10)

        assertEquals(10, result.size)
        assertEquals("weijian_backup_12.bin", result.first().fileName)
        assertEquals("weijian_backup_3.bin", result.last().fileName)
    }
}
