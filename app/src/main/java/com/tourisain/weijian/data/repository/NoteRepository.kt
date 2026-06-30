package com.tourisain.weijian.data.repository

import com.tourisain.weijian.data.database.dao.NoteDao
import com.tourisain.weijian.data.database.dao.NoteRevisionDao
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.data.database.entity.NoteRevisionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import com.tourisain.weijian.data.database.dao.CategoryCount

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteRevisionDao: NoteRevisionDao
) {
    fun getAllNotes(userId: String): Flow<List<NoteEntity>> {
        return noteDao.getAllNotes(userId)
    }

    fun getNotesPaged(userId: String, limit: Int, offset: Int): Flow<List<NoteEntity>> {
        return noteDao.getNotesPaged(userId, limit, offset)
    }

    suspend fun getNotesPagedSync(userId: String, limit: Int, offset: Int): List<NoteEntity> {
        return noteDao.getNotesPagedSync(userId, limit, offset)
    }

    suspend fun getNoteById(id: String, userId: String): Result<NoteEntity?> {
        return try {
            Result.success(noteDao.getNoteById(id, userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun searchNotes(userId: String, query: String): Flow<List<NoteEntity>> {
        return noteDao.searchNotes(userId, query)
    }

    fun searchNotesByCategory(userId: String, categoryId: String, query: String): Flow<List<NoteEntity>> {
        return noteDao.searchNotesByCategory(userId, categoryId, query)
    }

    fun searchUncategorizedNotes(userId: String, query: String): Flow<List<NoteEntity>> {
        return noteDao.searchUncategorizedNotes(userId, query)
    }

    suspend fun insertNote(note: NoteEntity): Result<Unit> {
        return try {
            noteDao.insertNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: NoteEntity): Result<Unit> {
        return try {
            val current = noteDao.getNoteById(note.id, note.userId)
            if (current != null && current.hasRevisionChange(note)) {
                noteRevisionDao.insertRevision(current.toRevision())
                noteRevisionDao.trimRevisions(current.id, current.userId, MAX_NOTE_REVISIONS)
            }
            noteDao.updateNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNoteRevisions(noteId: String, userId: String): Flow<List<NoteRevisionEntity>> {
        return noteRevisionDao.getRevisions(noteId, userId)
    }

    suspend fun restoreRevision(revisionId: String, noteId: String, userId: String): Result<Unit> {
        return try {
            val revision = noteRevisionDao.getRevision(revisionId, noteId, userId)
                ?: return Result.failure(IllegalArgumentException("Revision not found"))
            val current = noteDao.getNoteById(noteId, userId)
                ?: return Result.failure(IllegalArgumentException("Note not found"))
            if (current.hasRevisionChange(revision)) {
                noteRevisionDao.insertRevision(current.toRevision())
            }
            noteDao.updateNote(
                current.copy(
                    title = revision.title,
                    content = revision.content,
                    color = revision.color,
                    isPinned = revision.isPinned,
                    skinId = revision.skinId,
                    createdAt = revision.createdAt,
                    format = revision.format,
                    tags = revision.tags,
                    type = revision.type,
                    attachments = revision.attachments,
                    categoryId = revision.categoryId,
                    isFavorite = revision.isFavorite,
                    isArchived = revision.isArchived,
                    isLocked = revision.isLocked
                )
            )
            noteRevisionDao.trimRevisions(noteId, userId, MAX_NOTE_REVISIONS)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(note: NoteEntity): Result<Unit> {
        return try {
            noteDao.updateNote(
                note.copy(
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis(),
                    isArchived = false
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDeletedNotes(userId: String): Flow<List<NoteEntity>> {
        return noteDao.getDeletedNotes(userId)
    }

    suspend fun restoreNote(note: NoteEntity): Result<Unit> {
        return try {
            noteDao.updateNote(
                note.copy(
                    isDeleted = false,
                    deletedAt = null
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun permanentDeleteNote(note: NoteEntity): Result<Unit> {
        return try {
            noteRevisionDao.deleteRevisionsForNote(note.id, note.userId)
            noteDao.deleteNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearRecycleBin(userId: String): Result<Unit> {
        return try {
            noteDao.deleteRecycleBin(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearExpiredRecycleBin(timestamp: Long): Result<Unit> {
        return try {
            noteDao.deleteOldDeletedNotes(timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllNotes(userId: String): Result<Unit> {
        return try {
            noteDao.deleteAll(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNoteCount(userId: String): Flow<Int> {
        return noteDao.getNoteCount(userId)
    }

    fun getRecentNotes(userId: String, limit: Int): Flow<List<NoteEntity>> {
        return noteDao.getRecentNotes(userId, limit)
    }

    fun getNotesByCategory(userId: String, categoryId: String): Flow<List<NoteEntity>> {
        return noteDao.getNotesByCategory(userId, categoryId)
    }

    suspend fun getNotesByCategorySync(userId: String, categoryId: String): List<NoteEntity> {
        return noteDao.getNotesByCategorySync(userId, categoryId)
    }

    fun getNoteCountByCategory(userId: String, categoryId: String): Flow<Int> {
        return noteDao.getNoteCountByCategory(userId, categoryId)
    }

    suspend fun getNoteCountByCategories(userId: String): List<CategoryCount> {
        return noteDao.getNoteCountByCategories(userId)
    }

    fun getArchivedNotes(userId: String): Flow<List<NoteEntity>> {
        return noteDao.getArchivedNotes(userId)
    }

    private fun NoteEntity.toRevision(): NoteRevisionEntity {
        return NoteRevisionEntity(
            noteId = id,
            userId = userId,
            title = title,
            content = content,
            color = color,
            isPinned = isPinned,
            skinId = skinId,
            createdAt = createdAt,
            savedAt = System.currentTimeMillis(),
            format = format,
            tags = tags,
            type = type,
            attachments = attachments,
            categoryId = categoryId,
            isFavorite = isFavorite,
            isArchived = isArchived,
            isLocked = isLocked
        )
    }

    private fun NoteEntity.hasRevisionChange(other: NoteEntity): Boolean {
        return title != other.title ||
            content != other.content ||
            color != other.color ||
            isPinned != other.isPinned ||
            skinId != other.skinId ||
            format != other.format ||
            tags != other.tags ||
            type != other.type ||
            attachments != other.attachments ||
            categoryId != other.categoryId ||
            isFavorite != other.isFavorite ||
            isArchived != other.isArchived ||
            isLocked != other.isLocked
    }

    private fun NoteEntity.hasRevisionChange(revision: NoteRevisionEntity): Boolean {
        return title != revision.title ||
            content != revision.content ||
            color != revision.color ||
            isPinned != revision.isPinned ||
            skinId != revision.skinId ||
            format != revision.format ||
            tags != revision.tags ||
            type != revision.type ||
            attachments != revision.attachments ||
            categoryId != revision.categoryId ||
            isFavorite != revision.isFavorite ||
            isArchived != revision.isArchived ||
            isLocked != revision.isLocked
    }

    private companion object {
        const val MAX_NOTE_REVISIONS = 20
    }
}
