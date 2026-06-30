package com.tourisain.weijian.presentation.note.list

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteListQueryPlanTest {
    @Test
    fun normalSearchUsesDatabaseSearchInsteadOfFullListFiltering() {
        assertEquals(
            NoteListDataSource.SearchAllNotes,
            noteListDataSourceFor(ALL_NOTES_ROUTE, "meeting")
        )
        assertEquals(
            NoteListDataSource.SearchCategoryNotes,
            noteListDataSourceFor("work", "meeting")
        )
        assertEquals(
            NoteListDataSource.SearchUncategorizedNotes,
            noteListDataSourceFor(UNCATEGORIZED_ROUTE, "meeting")
        )
    }

    @Test
    fun tagSearchKeepsFullListBecauseTagsAreStoredAsJson() {
        assertEquals(
            NoteListDataSource.AllNotes,
            noteListDataSourceFor(ALL_NOTES_ROUTE, "#idea")
        )
        assertEquals(
            NoteListDataSource.CategoryNotes,
            noteListDataSourceFor("work", "#idea")
        )
    }

    @Test
    fun todoSmartFilterHasStableRouteAliases() {
        assertEquals(NoteSmartFilter.TODO, noteSmartFilterFromRoute("todo"))
        assertEquals(NoteSmartFilter.TODO, noteSmartFilterFromRoute("checklist"))
    }
}
