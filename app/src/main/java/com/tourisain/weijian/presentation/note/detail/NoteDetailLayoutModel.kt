package com.tourisain.weijian.presentation.note.detail

import kotlin.math.ceil

internal enum class EditorToolbarPrimaryAction {
    Undo,
    Redo,
    FontDecrease,
    FontIncrease,
    Format,
    HideKeyboard
}

internal data class EditorToolbarPrimaryActionItem(
    val action: EditorToolbarPrimaryAction,
    val label: String
)

internal enum class EditorToolbarFormatAction {
    Checklist,
    Heading,
    List,
    Bold,
    Italic,
    Strikethrough,
    Quote,
    Code,
    Divider,
    Image,
    Link,
    Table,
    Attachment,
    Delete
}

internal data class EditorToolbarFormatActionItem(
    val action: EditorToolbarFormatAction,
    val label: String
)

internal enum class EditorToolbarFormatGroupKey {
    Structure,
    Style,
    Insert
}

internal data class EditorToolbarFormatGroup(
    val key: EditorToolbarFormatGroupKey,
    val label: String,
    val actions: List<EditorToolbarFormatActionItem>
)

internal const val EDITOR_PAPER_HORIZONTAL_PADDING_DP = 2
internal const val NOTE_METADATA_ROW_VERTICAL_PADDING_DP = 7
internal const val EDITOR_TOOLBAR_BUTTON_SIZE_DP = 38
internal const val EDITOR_TOOLBAR_LAYER_GAP_DP = 4
internal const val EDITOR_CONTENT_BASE_BOTTOM_PADDING_DP = 12

internal fun editorToolbarImeOffsetPx(imeBottomPx: Int): Int {
    return -imeBottomPx.coerceAtLeast(0)
}

internal fun editorContentImeAvoidancePaddingDp(imeBottomPx: Int, density: Float): Int {
    if (imeBottomPx <= 0 || density <= 0f) return 0
    return ceil(imeBottomPx / density).toInt()
}

internal fun editorToolbarCollapsedHeightDp(): Int {
    return EDITOR_TOOLBAR_BUTTON_SIZE_DP + EDITOR_TOOLBAR_VERTICAL_PADDING_DP * 2
}

internal fun editorToolbarExpandedHeightDp(): Int {
    return editorToolbarCollapsedHeightDp() + EDITOR_TOOLBAR_LAYER_GAP_DP + EDITOR_TOOLBAR_BUTTON_SIZE_DP
}

internal fun editorContentBottomPaddingDp(
    imeBottomPx: Int,
    density: Float,
    isToolbarExpanded: Boolean = false
): Int {
    val toolbarHeight = if (isToolbarExpanded) {
        editorToolbarExpandedHeightDp()
    } else {
        editorToolbarCollapsedHeightDp()
    }
    return EDITOR_CONTENT_BASE_BOTTOM_PADDING_DP +
        toolbarHeight +
        editorContentImeAvoidancePaddingDp(imeBottomPx, density)
}

internal fun editorToolbarPrimaryActions(): List<EditorToolbarPrimaryActionItem> {
    return listOf(
        EditorToolbarPrimaryActionItem(EditorToolbarPrimaryAction.Undo, "\u64a4\u9500"),
        EditorToolbarPrimaryActionItem(EditorToolbarPrimaryAction.Redo, "\u91cd\u505a"),
        EditorToolbarPrimaryActionItem(EditorToolbarPrimaryAction.FontDecrease, "\u51cf\u5c0f"),
        EditorToolbarPrimaryActionItem(EditorToolbarPrimaryAction.FontIncrease, "\u589e\u5927"),
        EditorToolbarPrimaryActionItem(EditorToolbarPrimaryAction.Format, "\u683c\u5f0f"),
        EditorToolbarPrimaryActionItem(EditorToolbarPrimaryAction.HideKeyboard, "\u952e\u76d8")
    )
}

internal fun editorToolbarFormatActions(): List<EditorToolbarFormatActionItem> {
    return editorToolbarFormatGroups().flatMap { it.actions }
}

internal fun editorToolbarFormatGroups(): List<EditorToolbarFormatGroup> {
    return listOf(
        EditorToolbarFormatGroup(
            key = EditorToolbarFormatGroupKey.Structure,
            label = "\u7ed3\u6784",
            actions = listOf(
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Heading, "\u6807\u9898"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Checklist, "\u5f85\u529e"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.List, "\u5217\u8868"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Quote, "\u5f15\u7528"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Code, "\u4ee3\u7801")
            )
        ),
        EditorToolbarFormatGroup(
            key = EditorToolbarFormatGroupKey.Style,
            label = "\u6837\u5f0f",
            actions = listOf(
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Bold, "\u7c97\u4f53"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Italic, "\u659c\u4f53"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Strikethrough, "\u5220\u7ebf")
            )
        ),
        EditorToolbarFormatGroup(
            key = EditorToolbarFormatGroupKey.Insert,
            label = "\u63d2\u5165",
            actions = listOf(
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Image, "\u56fe\u7247"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Link, "\u94fe\u63a5"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Table, "\u8868\u683c"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Divider, "\u5206\u5272"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Attachment, "\u9644\u4ef6"),
                EditorToolbarFormatActionItem(EditorToolbarFormatAction.Delete, "\u5220\u9664")
            )
        )
    )
}

internal fun noteMetadataSummary(
    folderName: String?,
    tagCount: Int,
    attachmentCount: Int
): String {
    val parts = mutableListOf<String>()
    val normalizedFolderName = folderName?.trim().orEmpty()
    parts += normalizedFolderName.ifBlank { "\u672a\u5206\u7c7b" }
    if (tagCount > 0) parts += "${tagCount}\u6807\u7b7e"
    if (attachmentCount > 0) parts += "${attachmentCount}\u9644\u4ef6"
    return parts.joinToString(" \u00b7 ")
}
