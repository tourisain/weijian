package com.tourisain.weijian.data.repository

import com.tourisain.weijian.data.database.dao.AccountDao
import com.tourisain.weijian.data.database.dao.CategoryDao
import com.tourisain.weijian.data.database.entity.CategoryEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) {
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories(userId)
    }

    fun getMemoCategories(userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(userId, "MEMO")
    }

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity, previousName: String? = null) {
        categoryDao.updateCategory(category)
        if (
            previousName != null &&
            previousName != category.name &&
            category.type in ACCOUNT_CATEGORY_TYPES
        ) {
            accountDao.renameCategory(category.userId, category.type, previousName, category.name)
        }
    }

    suspend fun findCategory(userId: String, name: String, type: String): CategoryEntity? {
        return categoryDao.getCategoryByNameAndType(userId, name, type)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getAllCategoriesForSync(): List<CategoryEntity> {
        return categoryDao.getAllCategoriesForSync()
    }

    private companion object {
        val ACCOUNT_CATEGORY_TYPES = setOf("income", "expense")
    }
}
