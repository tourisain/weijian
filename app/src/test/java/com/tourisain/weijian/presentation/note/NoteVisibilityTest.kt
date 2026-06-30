package com.tourisain.weijian.presentation.note

import com.tourisain.weijian.data.database.entity.NoteEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteVisibilityTest {
    @Test
    fun normalTextNoteIsVisibleInNoteLists() {
        assertTrue(baseNote().isVisibleStandaloneNote())
    }

    @Test
    fun archivedNoteIsHiddenFromNormalNoteLists() {
        assertFalse(baseNote().copy(isArchived = true).isVisibleStandaloneNote())
    }

    @Test
    fun legacyDiaryMarkersAreNotStandaloneNotes() {
        assertFalse(baseNote().copy(type = "diary").isVisibleStandaloneNote())
        assertFalse(baseNote(id = "diary_123").isVisibleStandaloneNote())
    }

    private fun baseNote(id: String = "note_1") = NoteEntity(
        id = id,
        userId = "user",
        title = "Title",
        content = "Content",
        color = "#FFFFFF"
    )
}
