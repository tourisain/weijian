package com.tourisain.weijian.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tourisain.weijian.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>
    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<UserEntity>
    @Query("UPDATE users SET username = :username WHERE id = :id")
    suspend fun updateUsername(id: String, username: String): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    @Update
    suspend fun updateUser(user: UserEntity)
    @Delete
    suspend fun deleteUser(user: UserEntity)
    @Query("UPDATE users SET is_pro = 0, pro_expire_date = NULL, membership_level = 0")
    suspend fun clearAllMemberships()
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
