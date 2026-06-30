package com.tourisain.weijian.presentation.note.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppleNotesStyleTest {
    @Test
    fun visualRhythmUsesCompactAppleNotesMetrics() {
        assertEquals(12, AppleNotesStyle.GroupRadiusDp)
        assertEquals(10, AppleNotesStyle.SearchRadiusDp)
        assertEquals(10, AppleNotesStyle.SettingsRowVerticalPaddingDp)
        assertEquals(10, AppleNotesStyle.NoteRowVerticalPaddingDp)
        assertEquals(30, AppleNotesStyle.ListIconSizeDp)
        assertEquals(17, AppleNotesStyle.ListIconGlyphSizeDp)
        assertTrue(AppleNotesStyle.DialogMaxHeightDp <= 320)
    }
}
