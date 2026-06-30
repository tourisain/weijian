package com.tourisain.weijian.presentation.note.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDetailTopActionsTest {
    @Test
    fun topActionLabelsStayShortAndComplete() {
        val labels = noteDetailTopActions(isPinned = false, isLocked = false).map { it.label }

        assertEquals(listOf("置顶", "加锁", "历史", "分享", "导出"), labels)
        assertTrue(labels.all { it.length <= 2 })
    }

    @Test
    fun topActionLabelsReflectPinnedAndLockedState() {
        val labels = noteDetailTopActions(isPinned = true, isLocked = true).map { it.label }

        assertEquals(listOf("取消", "解锁", "历史", "分享", "导出"), labels)
        assertTrue(labels.all { it.length <= 2 })
    }

    @Test
    fun editorToolbarPaddingStaysCompactNearKeyboard() {
        assertTrue(EDITOR_TOOLBAR_VERTICAL_PADDING_DP <= 5)
    }

    @Test
    fun editorToolbarMovesByImeHeightInsteadOfAddingBlankPadding() {
        assertEquals(-320, editorToolbarImeOffsetPx(320))
        assertEquals(0, editorToolbarImeOffsetPx(0))
        assertEquals(0, editorToolbarImeOffsetPx(-1))
    }

    @Test
    fun editorContentKeepsBottomTextAboveLiftedToolbar() {
        assertEquals(160, editorContentImeAvoidancePaddingDp(320, density = 2f))
        assertEquals(107, editorContentImeAvoidancePaddingDp(320, density = 3f))
        assertEquals(0, editorContentImeAvoidancePaddingDp(0, density = 2f))
        assertEquals(0, editorContentImeAvoidancePaddingDp(320, density = 0f))
    }

    @Test
    fun editorContentBottomPaddingAlwaysAccountsForToolbar() {
        assertEquals(58, editorContentBottomPaddingDp(imeBottomPx = 0, density = 2f))
        assertEquals(218, editorContentBottomPaddingDp(imeBottomPx = 320, density = 2f))
        assertEquals(165, editorContentBottomPaddingDp(imeBottomPx = 320, density = 3f))
        assertEquals(100, editorContentBottomPaddingDp(imeBottomPx = 0, density = 2f, isToolbarExpanded = true))
    }

    @Test
    fun editorToolbarPrimaryActionsKeepWritingRowFocused() {
        val labels = editorToolbarPrimaryActions().map { it.label }

        assertEquals(
            listOf(
                "\u64a4\u9500",
                "\u91cd\u505a",
                "\u51cf\u5c0f",
                "\u589e\u5927",
                "\u683c\u5f0f",
                "\u952e\u76d8"
            ),
            labels
        )
        assertTrue(labels.all { it.length <= 2 })
    }

    @Test
    fun noteMetadataSummaryStaysCompact() {
        assertEquals(
            "\u5de5\u4f5c \u00b7 2\u6807\u7b7e \u00b7 1\u9644\u4ef6",
            noteMetadataSummary(folderName = "\u5de5\u4f5c", tagCount = 2, attachmentCount = 1)
        )
        assertEquals(
            "\u672a\u5206\u7c7b",
            noteMetadataSummary(folderName = "  ", tagCount = 0, attachmentCount = 0)
        )
    }

    @Test
    fun minimalWritingSurfaceUsesQuietPaperRules() {
        assertTrue(EDITOR_PAPER_HORIZONTAL_PADDING_DP <= 2)
        assertTrue(NOTE_METADATA_ROW_VERTICAL_PADDING_DP <= 7)
        assertTrue(EDITOR_TOOLBAR_BUTTON_SIZE_DP <= 38)
        assertTrue(EDITOR_TOOLBAR_LAYER_GAP_DP <= 4)
    }

    @Test
    fun formatActionsStayOutOfPrimaryWritingRow() {
        val labels = editorToolbarFormatActions().map { it.label }

        assertEquals(
            listOf(
                "\u6807\u9898",
                "\u5f85\u529e",
                "\u5217\u8868",
                "\u5f15\u7528",
                "\u4ee3\u7801",
                "\u7c97\u4f53",
                "\u659c\u4f53",
                "\u5220\u7ebf",
                "\u5206\u5272",
                "\u9644\u4ef6",
                "\u5220\u9664"
            ),
            labels
        )
        assertTrue(labels.all { it.length <= 2 })
    }
}
