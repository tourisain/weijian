package com.tourisain.weijian.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM account_records WHERE user_id = :userId AND is_deleted = 0 ORDER BY date DESC")
    fun getAllRecords(userId: String): Flow<List<AccountRecordEntity>>

    @Query("SELECT * FROM account_records WHERE user_id = :userId AND is_deleted = 0 ORDER BY date DESC")
    suspend fun getAllRecordsSync(userId: String): List<AccountRecordEntity>

    @Query("SELECT COUNT(*) FROM account_records WHERE user_id = :userId AND is_deleted = 0")
    suspend fun getRecordCount(userId: String): Int

    @Query("SELECT * FROM account_records WHERE id = :id AND user_id = :userId AND is_deleted = 0")
    suspend fun getRecordById(id: String, userId: String): AccountRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AccountRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AccountRecordEntity>)

    @Update
    suspend fun updateRecord(record: AccountRecordEntity)

    @Delete
    suspend fun deleteRecord(record: AccountRecordEntity)

    @Query("SELECT SUM(amount) FROM account_records WHERE user_id = :userId AND type = 'income' AND is_deleted = 0")
    fun getTotalIncome(userId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM account_records WHERE user_id = :userId AND type = 'expense' AND is_deleted = 0")
    fun getTotalExpense(userId: String): Flow<Double?>

    @Query("DELETE FROM account_records WHERE user_id = :userId")
    suspend fun deleteAll(userId: String)

    @Query("SELECT * FROM account_records ORDER BY date DESC")
    suspend fun getAllRecordsForSync(): List<AccountRecordEntity>

    @Query("SELECT * FROM account_records WHERE user_id = :userId AND is_deleted = 0 AND (note LIKE :query OR category LIKE :query) ORDER BY date DESC")
    fun searchRecords(userId: String, query: String): Flow<List<AccountRecordEntity>>

    @Query("SELECT * FROM account_records WHERE user_id = :userId AND is_deleted = 0 ORDER BY date DESC LIMIT :limit")
    fun getRecentAccountRecords(userId: String, limit: Int): Flow<List<AccountRecordEntity>>

    @Query("UPDATE account_records SET category = :newName WHERE user_id = :userId AND type = :type AND category = :oldName")
    suspend fun renameCategory(userId: String, type: String, oldName: String, newName: String)

    @Query("SELECT * FROM account_records WHERE user_id = :userId AND is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeletedRecords(userId: String): Flow<List<AccountRecordEntity>>

    @Query("DELETE FROM account_records WHERE user_id = :userId AND is_deleted = 1")
    suspend fun deleteRecycleBin(userId: String)

    @Query("DELETE FROM account_records WHERE is_deleted = 1 AND deleted_at < :timestamp")
    suspend fun deleteOldDeletedRecords(timestamp: Long)
}
