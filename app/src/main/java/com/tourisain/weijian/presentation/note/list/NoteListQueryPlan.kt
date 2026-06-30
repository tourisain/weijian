package com.tourisain.weijian.presentation.note.list

internal enum class NoteListDataSource {
    AllNotes,
    UncategorizedNotes,
    CategoryNotes,
    SearchAllNotes,
    SearchUncategorizedNotes,
    SearchCategoryNotes
}

internal fun noteListDataSourceFor(
    categoryRoute: String,
    query: String
): NoteListDataSource {
    val trimmedQuery = query.trim()
    val canUseDatabaseSearch = trimmedQuery.isNotBlank() && !trimmedQuery.startsWith("#")
    return when {
        categoryRoute == ALL_NOTES_ROUTE && canUseDatabaseSearch -> NoteListDataSource.SearchAllNotes
        categoryRoute == UNCATEGORIZED_ROUTE && canUseDatabaseSearch -> NoteListDataSource.SearchUncategorizedNotes
        canUseDatabaseSearch -> NoteListDataSource.SearchCategoryNotes
        categoryRoute == ALL_NOTES_ROUTE -> NoteListDataSource.AllNotes
        categoryRoute == UNCATEGORIZED_ROUTE -> NoteListDataSource.UncategorizedNotes
        else -> NoteListDataSource.CategoryNotes
    }
}

internal fun NoteListDataSource.usesDatabaseSearch(): Boolean {
    return this == NoteListDataSource.SearchAllNotes ||
        this == NoteListDataSource.SearchUncategorizedNotes ||
        this == NoteListDataSource.SearchCategoryNotes
}
