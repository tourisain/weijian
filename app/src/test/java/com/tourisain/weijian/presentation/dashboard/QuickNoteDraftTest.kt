package com.tourisain.weijian.presentation.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickNoteDraftTest {
    @Test
    fun quickDraftUsesFirstContentLineAsTitleWhenTitleIsBlank() {
        val draft = quickNoteDraft(title = "", content = "Buy milk\nbefore dinner")

        assertEquals("Buy milk", draft?.title)
        assertEquals("Buy milk\nbefore dinner", draft?.content)
    }

    @Test
    fun quickDraftRejectsCompletelyBlankInput() {
        assertNull(quickNoteDraft(title = " ", content = "\n "))
    }
}
