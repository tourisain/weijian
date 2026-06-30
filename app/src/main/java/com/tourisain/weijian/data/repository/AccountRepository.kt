package com.tourisain.weijian.data.repository

import com.tourisain.weijian.data.database.dao.AccountDao
import com.tourisain.weijian.data.database.dao.CategoryDao
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) {
    fun getAllRecords(userId: String): Flow<List<AccountRecordEntity>> {
        return accountDao.getAllRecords(userId)
    }
    fun getTotalIncome(userId: String): Flow<Double?> {
        return accountDao.getTotalIncome(userId)
    }
    fun getTotalExpense(userId: String): Flow<Double?> {
        return accountDao.getTotalExpense(userId)
    }
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories(userId)
    }
    fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(userId, type)
    }
    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }
    
    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
    suspend fun getRecordById(id: String, userId: String): AccountRecordEntity? {
        return accountDao.getRecordById(id, userId)
    }
    suspend fun getRecordCount(userId: String): Int {
        return accountDao.getRecordCount(userId)
    }
    fun searchRecords(userId: String, query: String): Flow<List<AccountRecordEntity>> {
        return accountDao.searchRecords(userId, "%$query%")
    }
    suspend fun insertRecord(record: AccountRecordEntity) {
        accountDao.insertRecord(record)
    }
    suspend fun updateRecord(record: AccountRecordEntity) {
        accountDao.updateRecord(record)
    }
    suspend fun deleteRecord(record: AccountRecordEntity) {
        accountDao.updateRecord(
            record.copy(
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
        )
    }
    fun getDeletedRecords(userId: String): Flow<List<AccountRecordEntity>> {
        return accountDao.getDeletedRecords(userId)
    }
    suspend fun restoreRecord(record: AccountRecordEntity) {
        accountDao.updateRecord(
            record.copy(
                isDeleted = false,
                deletedAt = null
            )
        )
    }
    suspend fun permanentDeleteRecord(record: AccountRecordEntity) {
        accountDao.deleteRecord(record)
    }
    suspend fun clearRecycleBin(userId: String) {
        accountDao.deleteRecycleBin(userId)
    }
    suspend fun clearExpiredRecycleBin(timestamp: Long) {
        accountDao.deleteOldDeletedRecords(timestamp)
    }
    // 用于同步的方法，不需要 userId 过滤
    suspend fun getAllRecordsForSync(): List<AccountRecordEntity> {
        return accountDao.getAllRecordsForSync()
    }
    // 获取最近的记账记录
    fun getRecentAccountRecords(userId: String, limit: Int): Flow<List<AccountRecordEntity>> {
        return accountDao.getRecentAccountRecords(userId, limit)
    }
}
