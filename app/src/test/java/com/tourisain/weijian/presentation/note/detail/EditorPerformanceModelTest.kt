package com.tourisain.weijian.presentation.note.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorPerformanceModelTest {
    @Test
    fun richRenderingWaitsUntilTypingSettles() {
        val value = TextFieldValue("**hello**", TextRange(9))

        assertFalse(
            shouldUseRichRendering(
                value = value,
                hasRichMarker = true,
                nowMs = 1_000L,
                lastInputAtMs = 820L
            )
        )
        assertTrue(
            shouldUseRichRendering(
                value = value,
                hasRichMarker = true,
                nowMs = 1_400L,
                lastInputAtMs = 820L
            )
        )
    }

    @Test
    fun richRenderingStaysOffForLargeTextOrLargeSelection() {
        val longValue = TextFieldValue("# ".repeat(10_000), TextRange(0))
        val largeSelection = TextFieldValue("**hello**".repeat(800), TextRange(0, 4_000))

        assertFalse(shouldUseRichRendering(longValue, hasRichMarker = true, nowMs = 2_000L, lastInputAtMs = 0L))
        assertFalse(shouldUseRichRendering(largeSelection, hasRichMarker = true, nowMs = 2_000L, lastInputAtMs = 0L))
    }

    @Test
    fun viewModelSyncIsDeferredForLargeTypingBursts() {
        assertTrue(
            shouldDeferEditorSync(
                previousTextLength = 50_000,
                nextTextLength = 50_001,
                selectionLength = 0,
                hasComposition = false
            )
        )
        assertFalse(
            shouldDeferEditorSync(
                previousTextLength = 50_000,
                nextTextLength = 50_400,
                selectionLength = 0,
                hasComposition = false
            )
        )
        assertFalse(
            shouldDeferEditorSync(
                previousTextLength = 10,
                nextTextLength = 11,
                selectionLength = 0,
                hasComposition = false
            )
        )
    }

    @Test
    fun editorInteractionStateDetectsTypingSelectingAndRecovering() {
        val typing = TextFieldValue("hello", TextRange(5))
        val selecting = TextFieldValue("hello", TextRange(0, 5))

        assertEquals(
            EditorInteractionState.Typing,
            editorInteractionState(typing, nowMs = 1_000L, lastInputAtMs = 900L)
        )
        assertEquals(
            EditorInteractionState.Selecting,
            editorInteractionState(selecting, nowMs = 1_000L, lastInputAtMs = 900L)
        )
        assertEquals(
            EditorInteractionState.Recovering,
            editorInteractionState(typing, nowMs = 1_000L, lastInputAtMs = 900L, isRecovering = true)
        )
    }

    @Test
    fun typingAssistStaysOffDuringSelectionFormattingAndRecovery() {
        assertTrue(shouldApplyTypingAssist(EditorInteractionState.Typing))
        assertFalse(shouldApplyTypingAssist(EditorInteractionState.Selecting))
        assertFalse(shouldApplyTypingAssist(EditorInteractionState.Formatting))
        assertFalse(shouldApplyTypingAssist(EditorInteractionState.Recovering))
    }

    @Test
    fun ultraLongTextUsesProtectiveSyncDelay() {
        assertEquals(220L, editorSyncDelayMs(textLength = LARGE_TEXT_SYNC_DEFER_LENGTH))
        assertEquals(420L, editorSyncDelayMs(textLength = 80_000))
    }

    @Test
    fun ultraLongTextDefersModerateEditsToKeepInputResponsive() {
        assertTrue(
            shouldDeferEditorSync(
                previousTextLength = 100_000,
                nextTextLength = 100_800,
                selectionLength = 0,
                hasComposition = false
            )
        )
        assertFalse(
            shouldDeferEditorSync(
                previousTextLength = 100_000,
                nextTextLength = 102_000,
                selectionLength = 0,
                hasComposition = false
            )
        )
    }

    @Test
    fun editorTransactionOnlyCommitsRealChanges() {
        val original = TextFieldValue("hello", TextRange(5))
        val changedSelection = TextFieldValue("hello", TextRange(0, 5))

        assertFalse(shouldCommitEditorTransaction(original, original))
        assertTrue(shouldCommitEditorTransaction(original, changedSelection))
    }

    @Test
    fun editorTextSyncSkipsSelectionOnlyChanges() {
        assertFalse(shouldSyncEditorText(previousText = "hello", nextText = "hello"))
        assertTrue(shouldSyncEditorText(previousText = "hello", nextText = "hello!"))
    }

    @Test
    fun primaryToolbarKeepsOnlyLowDistractionActions() {
        val actions = editorToolbarPrimaryActions().map { it.action }

        assertEquals(
            listOf(
                EditorToolbarPrimaryAction.Undo,
                EditorToolbarPrimaryAction.Redo,
                EditorToolbarPrimaryAction.FontDecrease,
                EditorToolbarPrimaryAction.FontIncrease,
                EditorToolbarPrimaryAction.Format,
                EditorToolbarPrimaryAction.HideKeyboard
            ),
            actions
        )
        assertTrue(editorToolbarFormatActions().any { it.action == EditorToolbarFormatAction.Checklist })
    }

    @Test
    fun formatToolbarIsGroupedForLowDistractionEditing() {
        val groups = editorToolbarFormatGroups()

        assertEquals(
            listOf(
                EditorToolbarFormatGroupKey.Structure,
                EditorToolbarFormatGroupKey.Style,
                EditorToolbarFormatGroupKey.Insert
            ),
            groups.map { it.key }
        )
        assertEquals(
            listOf(
                EditorToolbarFormatAction.Heading,
                EditorToolbarFormatAction.Checklist,
                EditorToolbarFormatAction.List,
                EditorToolbarFormatAction.Quote,
                EditorToolbarFormatAction.Code
            ),
            groups[0].actions.map { it.action }
        )
        assertEquals(EDITOR_TOOLBAR_BUTTON_SIZE_DP + EDITOR_TOOLBAR_VERTICAL_PADDING_DP * 2, editorToolbarCollapsedHeightDp())
    }
}
