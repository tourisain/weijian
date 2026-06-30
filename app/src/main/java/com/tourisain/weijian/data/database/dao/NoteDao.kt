package com.tourisain.weijian.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tourisain.weijian.data.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

data class CategoryCount(
    val category_id: String?,
    val count: Int
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 ORDER BY is_pinned DESC, created_at DESC")
    fun getAllNotes(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 ORDER BY is_pinned DESC, created_at DESC LIMIT :limit OFFSET :offset")
    fun getNotesPaged(userId: String, limit: Int, offset: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 ORDER BY is_pinned DESC, created_at DESC")
    suspend fun getAllNotesSync(userId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 ORDER BY is_pinned DESC, created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getNotesPagedSync(userId: String, limit: Int, offset: Int): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id AND user_id = :userId AND is_deleted = 0")
    suspend fun getNoteById(id: String, userId: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY is_pinned DESC, created_at DESC")
    fun searchNotes(userId: String, query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 AND category_id = :categoryId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY is_pinned DESC, created_at DESC")
    fun searchNotesByCategory(userId: String, categoryId: String, query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 AND (category_id IS NULL OR category_id = '') AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY is_pinned DESC, created_at DESC")
    fun searchUncategorizedNotes(userId: String, query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE user_id = :userId")
    suspend fun deleteAll(userId: String)

    @Query("SELECT COUNT(*) FROM notes WHERE user_id = :userId AND is_deleted = 0")
    fun getNoteCount(userId: String): Flow<Int>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    fun getRecentNotes(userId: String, limit: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, created_at DESC")
    suspend fun getAllNotesForSync(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND category_id = :categoryId AND is_deleted = 0 ORDER BY is_pinned DESC, created_at DESC")
    fun getNotesByCategory(userId: String, categoryId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND category_id = :categoryId AND is_deleted = 0 ORDER BY is_pinned DESC, created_at DESC")
    suspend fun getNotesByCategorySync(userId: String, categoryId: String): List<NoteEntity>

    @Query("SELECT COUNT(*) FROM notes WHERE user_id = :userId AND category_id = :categoryId AND is_deleted = 0")
    fun getNoteCountByCategory(userId: String, categoryId: String): Flow<Int>

    @Query("SELECT category_id, COUNT(*) as count FROM notes WHERE user_id = :userId AND is_deleted = 0 GROUP BY category_id")
    suspend fun getNoteCountByCategories(userId: String): List<CategoryCount>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_archived = 1 AND is_deleted = 0 ORDER BY created_at DESC")
    fun getArchivedNotes(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeletedNotes(userId: String): Flow<List<NoteEntity>>

    @Query("DELETE FROM notes WHERE user_id = :userId AND is_deleted = 1")
    suspend fun deleteRecycleBin(userId: String)

    @Query("DELETE FROM notes WHERE is_deleted = 1 AND deleted_at < :timestamp")
    suspend fun deleteOldDeletedNotes(timestamp: Long)
}
