package com.tourisain.weijian.presentation.backup

import com.tourisain.weijian.data.model.BackupData

data class BackupRestorePreview(
    val version: Int,
    val timestamp: Long,
    val sourceName: String? = null,
    val notes: Int,
    val noteRevisions: Int,
    val accountRecords: Int,
    val categories: Int,
    val noteCategories: Int,
    val appSettings: Int,
    val cardVisibility: Int
) {
    val totalItems: Int
        get() = notes +
            noteRevisions +
            accountRecords +
            categories +
            noteCategories +
            appSettings +
            cardVisibility

    val hasRestorableData: Boolean
        get() = totalItems > 0
}

fun backupRestorePreview(
    data: BackupData,
    sourceName: String? = null
): BackupRestorePreview {
    return BackupRestorePreview(
        version = data.version ?: 0,
        timestamp = data.timestamp ?: 0L,
        sourceName = sourceName,
        notes = data.notes?.size ?: 0,
        noteRevisions = data.noteRevisions?.size ?: 0,
        accountRecords = data.accountRecords?.size ?: 0,
        categories = data.categories?.size ?: 0,
        noteCategories = data.noteCategories?.size ?: 0,
        appSettings = data.appSettings?.size ?: 0,
        cardVisibility = data.cardVisibility?.size ?: 0
    )
}

fun backupRestorePreviewSummaryLines(preview: BackupRestorePreview): List<String> {
    val source = preview.sourceName?.takeIf { it.isNotBlank() }?.let { listOf("\u6765\u6e90 $it") }.orEmpty()
    return source + listOf(
        "\u7248\u672c ${preview.version}",
        "\u7b14\u8bb0 ${preview.notes} / \u5386\u53f2 ${preview.noteRevisions}",
        "\u8bb0\u8d26 ${preview.accountRecords} / \u5206\u7c7b ${preview.categories}",
        "\u7b14\u8bb0\u6587\u4ef6\u5939 ${preview.noteCategories} / \u8bbe\u7f6e ${preview.appSettings + preview.cardVisibility}",
        "\u603b\u9879\u76ee ${preview.totalItems}"
    )
}
