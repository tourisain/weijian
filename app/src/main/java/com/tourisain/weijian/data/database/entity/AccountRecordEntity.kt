package com.tourisain.weijian.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "account_records",
    indices = [
        Index(value = ["user_id"], name = "idx_account_records_user_id"),
        Index(value = ["user_id", "is_deleted"], name = "idx_account_records_user_deleted"),
        Index(value = ["user_id", "is_deleted", "date"], name = "idx_account_records_user_deleted_date"),
        Index(value = ["is_deleted", "deleted_at"], name = "idx_account_records_deleted_time")
    ]
)
data class AccountRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "type") // "income" or "expense"
    val type: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "note")
    val note: String = "",
    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)
