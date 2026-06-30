package com.tourisain.weijian.presentation.note.list

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteListVisualModelTest {
    @Test
    fun noteRowMetaUsesQuietMiddleDotSeparator() {
        assertEquals("09:30 · 工作", noteRowMetaText("09:30", "工作"))
        assertEquals("09:30", noteRowMetaText("09:30", " "))
    }
}
