package com.tourisain.weijian.presentation.backup

import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.database.entity.CategoryEntity
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.data.database.entity.NoteRevisionEntity
import com.tourisain.weijian.data.model.BackupData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestorePreviewTest {
    @Test
    fun backupRestorePreviewCountsAllRestorableCoreData() {
        val preview = backupRestorePreview(
            BackupData(
                version = 10,
                timestamp = 1_719_000_000_000L,
                notes = listOf(NoteEntity(userId = "u", title = "n", content = "c", color = "#fff")),
                noteRevisions = listOf(NoteRevisionEntity(noteId = "n", userId = "u", title = "old", content = "old")),
                accountRecords = listOf(AccountRecordEntity(userId = "u", amount = 1.0, type = "expense", category = "food")),
                categories = listOf(CategoryEntity(userId = "u", name = "work", type = "note")),
                noteCategories = listOf(NoteCategoryEntity(userId = "u", name = "work", color = "#fff")),
                appSettings = mapOf("appTheme" to "system", "dynamicTheme" to true),
                cardVisibility = mapOf("notes" to true)
            )
        )

        assertEquals(10, preview.version)
        assertEquals(1, preview.notes)
        assertEquals(1, preview.noteRevisions)
        assertEquals(1, preview.accountRecords)
        assertEquals(1, preview.categories)
        assertEquals(1, preview.noteCategories)
        assertEquals(2, preview.appSettings)
        assertEquals(1, preview.cardVisibility)
        assertEquals(8, preview.totalItems)
        assertTrue(preview.hasRestorableData)
    }

    @Test
    fun emptyBackupRestorePreviewIsNotRestorable() {
        val preview = backupRestorePreview(BackupData(version = 10, timestamp = 1L))

        assertEquals(0, preview.totalItems)
        assertFalse(preview.hasRestorableData)
    }

    @Test
    fun backupRestorePreviewSummaryUsesShortStableLines() {
        val lines = backupRestorePreviewSummaryLines(
            BackupRestorePreview(
                version = 10,
                timestamp = 1L,
                sourceName = "latest.bin",
                notes = 3,
                noteRevisions = 2,
                accountRecords = 1,
                categories = 4,
                noteCategories = 5,
                appSettings = 6,
                cardVisibility = 7
            )
        )

        assertEquals(
            listOf(
                "\u6765\u6e90 latest.bin",
                "\u7248\u672c 10",
                "\u7b14\u8bb0 3 / \u5386\u53f2 2",
                "\u8bb0\u8d26 1 / \u5206\u7c7b 4",
                "\u7b14\u8bb0\u6587\u4ef6\u5939 5 / \u8bbe\u7f6e 13",
                "\u603b\u9879\u76ee 28"
            ),
            lines
        )
    }

    @Test
    fun backupPreviewCanRepresentWebDavSourceBeforeRestore() {
        val preview = backupRestorePreview(
            data = BackupData(
                version = 10,
                timestamp = 1L,
                notes = listOf(NoteEntity(userId = "u", title = "n", content = "c", color = "#fff"))
            ),
            sourceName = "weijian_backup_1.bin"
        )

        assertEquals("weijian_backup_1.bin", preview.sourceName)
        assertTrue(preview.hasRestorableData)
    }
}
