package com.tourisain.weijian.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: NoteCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: NoteCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: NoteCategoryEntity)

    @Query("SELECT * FROM note_categories WHERE user_id = :userId ORDER BY sort_order ASC, created_at ASC")
    suspend fun getNoteCategories(userId: String): List<NoteCategoryEntity>

    @Query("SELECT * FROM note_categories WHERE id = :categoryId")
    suspend fun getNoteCategoryById(categoryId: String): NoteCategoryEntity?

    @Query("UPDATE note_categories SET sort_order = sort_order + 1 WHERE user_id = :userId AND sort_order >= :sortOrder")
    suspend fun incrementSortOrders(userId: String, sortOrder: Int)

    @Query("UPDATE note_categories SET sort_order = sort_order - 1 WHERE user_id = :userId AND sort_order > :sortOrder")
    suspend fun decrementSortOrders(userId: String, sortOrder: Int)

    @Query("DELETE FROM note_categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("UPDATE notes SET category_id = NULL WHERE category_id = :categoryId")
    suspend fun updateNotesCategoryToNull(categoryId: String)

    @Query("SELECT * FROM note_categories WHERE user_id = :userId ORDER BY sort_order ASC, created_at ASC")
    fun getAllCategories(userId: String): Flow<List<NoteCategoryEntity>>

    @Query("SELECT COUNT(*) FROM notes WHERE category_id = :categoryId AND is_deleted = 0")
    suspend fun getNoteCountByCategoryId(categoryId: String): Int
}
