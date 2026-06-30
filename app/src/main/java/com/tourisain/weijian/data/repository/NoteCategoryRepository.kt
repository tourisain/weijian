package com.tourisain.weijian.data.repository

import com.tourisain.weijian.data.database.dao.NoteCategoryDao
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteCategoryRepository @Inject constructor(
    private val noteCategoryDao: NoteCategoryDao
) {
    fun getAllCategories(userId: String): Flow<List<NoteCategoryEntity>> {
        return noteCategoryDao.getAllCategories(userId)
    }

    suspend fun createNoteCategory(category: NoteCategoryEntity): Long = withContext(Dispatchers.IO) {
        val nextOrder = noteCategoryDao.getNoteCategories(category.userId).size
        noteCategoryDao.insertCategory(category.copy(sortOrder = nextOrder))
    }

    suspend fun updateNoteCategory(category: NoteCategoryEntity) = withContext(Dispatchers.IO) {
        noteCategoryDao.updateCategory(category)
    }

    suspend fun deleteNoteCategory(category: NoteCategoryEntity) = withContext(Dispatchers.IO) {
        noteCategoryDao.updateNotesCategoryToNull(category.id)
        noteCategoryDao.deleteCategory(category)
    }

    suspend fun getNoteCategories(userId: String): List<NoteCategoryEntity> = withContext(Dispatchers.IO) {
        noteCategoryDao.getNoteCategories(userId)
    }

    suspend fun getNoteCategoryById(categoryId: String): NoteCategoryEntity? = withContext(Dispatchers.IO) {
        noteCategoryDao.getNoteCategoryById(categoryId)
    }

    suspend fun moveCategoryUp(userId: String, categoryId: String) = withContext(Dispatchers.IO) {
        val categories = noteCategoryDao.getNoteCategories(userId)
        val currentIndex = categories.indexOfFirst { it.id == categoryId }
        if (currentIndex > 0) {
            swapSortOrder(categories[currentIndex], categories[currentIndex - 1])
        }
    }

    suspend fun moveCategoryDown(userId: String, categoryId: String) = withContext(Dispatchers.IO) {
        val categories = noteCategoryDao.getNoteCategories(userId)
        val currentIndex = categories.indexOfFirst { it.id == categoryId }
        if (currentIndex in 0 until categories.lastIndex) {
            swapSortOrder(categories[currentIndex], categories[currentIndex + 1])
        }
    }

    suspend fun getNoteCountByCategoryId(categoryId: String): Int = withContext(Dispatchers.IO) {
        noteCategoryDao.getNoteCountByCategoryId(categoryId)
    }

    private suspend fun swapSortOrder(first: NoteCategoryEntity, second: NoteCategoryEntity) {
        noteCategoryDao.updateCategory(first.copy(sortOrder = second.sortOrder))
        noteCategoryDao.updateCategory(second.copy(sortOrder = first.sortOrder))
    }
}
