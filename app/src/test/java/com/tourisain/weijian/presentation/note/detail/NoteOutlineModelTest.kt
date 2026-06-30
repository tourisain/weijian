package com.tourisain.weijian.presentation.note.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteOutlineModelTest {
    @Test
    fun outlineUsesMarkdownHeadingsWithOffsetsAndLevels() {
        val content = "# Plan\nintro\n## Step one\nbody\n### Detail\nmore"

        val outline = noteOutlineItems(content)

        assertEquals(3, outline.size)
        assertEquals("Plan", outline[0].title)
        assertEquals(1, outline[0].level)
        assertEquals(0, outline[0].lineIndex)
        assertEquals(0, outline[0].startOffset)
        assertEquals("Step one", outline[1].title)
        assertEquals(2, outline[1].level)
        assertEquals(2, outline[1].lineIndex)
        assertEquals("Detail", outline[2].title)
        assertEquals(3, outline[2].level)
    }

    @Test
    fun collapsedSectionsHideUntilNextSameOrHigherHeading() {
        val content = "# Plan\nintro\n## Step one\nbody\n### Detail\nmore\n# Later\nlast"
        val outline = noteOutlineItems(content)

        val collapsed = noteOutlineItems(content, collapsedHeadingOffsets = setOf(outline[1].startOffset))

        assertEquals(false, collapsed[0].isCollapsed)
        assertEquals(true, collapsed[1].isCollapsed)
        assertEquals(3, collapsed[1].hiddenLineCount)
        assertEquals(false, collapsed[2].isCollapsed)
    }
}
