package com.tourisain.weijian.presentation.search

import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.database.entity.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchRecoveryFilterTest {
    @Test
    fun recoveryFilterKeepsTodosAndAttachmentsSeparate() {
        val notes = listOf(
            note(id = "todo", content = "- [ ] buy milk"),
            note(id = "attachment", attachments = "[\"content://file\"]"),
            note(id = "locked", isLocked = true, content = "- [ ] hidden")
        )
        val records = listOf(record("record"))

        val todos = filterSearchResults(SearchResults(notes, records), SearchRecoveryFilter.Todos)
        val attachments = filterSearchResults(SearchResults(notes, records), SearchRecoveryFilter.Attachments)
        val locked = filterSearchResults(SearchResults(notes, records), SearchRecoveryFilter.Locked)

        assertEquals(listOf("todo"), todos.notes.map { it.id })
        assertEquals(emptyList<AccountRecordEntity>(), todos.records)
        assertEquals(listOf("attachment"), attachments.notes.map { it.id })
        assertEquals(listOf("locked"), locked.notes.map { it.id })
    }

    @Test
    fun recoveryFilterCanFocusAccountsOnly() {
        val results = filterSearchResults(
            SearchResults(notes = listOf(note("note")), records = listOf(record("record"))),
            SearchRecoveryFilter.Accounts
        )

        assertEquals(emptyList<NoteEntity>(), results.notes)
        assertEquals(listOf("record"), results.records.map { it.id })
    }

    private fun note(
        id: String,
        content: String = "plain",
        attachments: String? = null,
        isLocked: Boolean = false
    ) = NoteEntity(
        id = id,
        userId = "user",
        title = id,
        content = content,
        color = "#FFFFFF",
        attachments = attachments,
        isLocked = isLocked
    )

    private fun record(id: String) = AccountRecordEntity(
        id = id,
        userId = "user",
        amount = 12.0,
        type = "expense",
        category = "food"
    )
}
