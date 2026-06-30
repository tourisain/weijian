package com.tourisain.weijian.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tourisain.weijian.data.database.entity.NoteRevisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteRevisionDao {
    @Query("SELECT * FROM note_revisions WHERE note_id = :noteId AND user_id = :userId ORDER BY saved_at DESC")
    fun getRevisions(noteId: String, userId: String): Flow<List<NoteRevisionEntity>>

    @Query("SELECT * FROM note_revisions WHERE id = :revisionId AND note_id = :noteId AND user_id = :userId LIMIT 1")
    suspend fun getRevision(revisionId: String, noteId: String, userId: String): NoteRevisionEntity?

    @Query("SELECT * FROM note_revisions ORDER BY saved_at DESC")
    suspend fun getAllRevisionsForSync(): List<NoteRevisionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: NoteRevisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisions(revisions: List<NoteRevisionEntity>)

    @Query("DELETE FROM note_revisions WHERE note_id = :noteId AND user_id = :userId")
    suspend fun deleteRevisionsForNote(noteId: String, userId: String)

    @Query(
        """
        DELETE FROM note_revisions
        WHERE note_id = :noteId
            AND user_id = :userId
            AND id NOT IN (
                SELECT id FROM note_revisions
                WHERE note_id = :noteId AND user_id = :userId
                ORDER BY saved_at DESC
                LIMIT :maxCount
            )
        """
    )
    suspend fun trimRevisions(noteId: String, userId: String, maxCount: Int)
}
