package com.tourisain.weijian.presentation.note.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorFormattingStabilityTest {
    @Test
    fun typingAssistContinuesNumberedListsAndExitsEmptyNumberedLine() {
        val numbered = TextFieldValue("1. hello", TextRange("1. hello".length))
        val afterEnter = applyEditorTypingAssist(
            numbered,
            TextFieldValue("1. hello\n", TextRange("1. hello\n".length))
        )

        assertEquals("1. hello\n2. ", afterEnter.text)
        assertEquals(TextRange("1. hello\n2. ".length), afterEnter.selection)

        val emptyNumbered = TextFieldValue("1. hello\n2. ", TextRange("1. hello\n2. ".length))
        val afterEmptyEnter = applyEditorTypingAssist(
            emptyNumbered,
            TextFieldValue("1. hello\n2. \n", TextRange("1. hello\n2. \n".length))
        )

        assertEquals("1. hello\n", afterEmptyEnter.text)
        assertEquals(TextRange("1. hello\n".length), afterEmptyEnter.selection)
    }

    @Test
    fun lineFormattingKeepsBlankLinesClean() {
        val value = TextFieldValue("first\n\nsecond", TextRange(0, "first\n\nsecond".length))

        val list = applyEditorAction(value, EditorAction.List)
        val quote = applyEditorAction(value, EditorAction.Quote)
        val heading = applyEditorAction(value, EditorAction.Heading)

        assertEquals("- first\n\n- second", list.text)
        assertEquals("> first\n\n> second", quote.text)
        assertEquals("# first\n\n# second", heading.text)
    }

    @Test
    fun italicKeepsSelectionOnOriginalText() {
        val value = TextFieldValue("hello world", TextRange(0, 5))

        val result = applyEditorAction(value, EditorAction.Italic)

        assertEquals("*hello* world", result.text)
        assertEquals(TextRange(1, 6), result.selection)
    }

    @Test
    fun codeBlockKeepsLargeSelectionOnOriginalText() {
        val selected = "line1\nline2\nline3"
        val value = TextFieldValue(selected, TextRange(0, selected.length))

        val result = applyEditorAction(value, EditorAction.Code)

        assertEquals("```\n$selected\n```", result.text)
        assertEquals(TextRange(4, 4 + selected.length), result.selection)
    }

    @Test
    fun checklistToggleAtCursorSwitchesOnlyCurrentChecklistLine() {
        val text = "- [ ] first\n- [x] second\nplain"

        val first = toggleChecklistAtCursor(TextFieldValue(text, TextRange(4)))
        assertEquals("- [x] first\n- [x] second\nplain", first.text)
        assertEquals(TextRange(4), first.selection)

        val second = toggleChecklistAtCursor(TextFieldValue(first.text, TextRange(18)))
        assertEquals("- [x] first\n- [ ] second\nplain", second.text)
    }

    @Test
    fun richPreviewUsesPolishedListChecklistAndQuoteGlyphs() {
        val rendered = RichNoteRenderer("- item\n- [ ] task\n- [x] done\n> quote").render().text.text

        assertTrue(rendered.contains("• item"))
        assertTrue(rendered.contains("☐ task"))
        assertTrue(rendered.contains("☑ done"))
        assertTrue(rendered.contains("│ quote"))
        assertFalse(rendered.contains("? item"))
        assertFalse(rendered.contains("? task"))
    }
}
