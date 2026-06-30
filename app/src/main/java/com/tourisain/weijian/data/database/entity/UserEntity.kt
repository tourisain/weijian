package com.tourisain.weijian.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    @ColumnInfo(name = "is_pro")
    val isPro: Boolean = false,
    @ColumnInfo(name = "pro_expire_date")
    val proExpireDate: Long? = null,
    @ColumnInfo(name = "membership_level")
    val membershipLevel: Int = 0, // 0: 普通用�? 1: 银卡会员, 2: 金卡会员, 3: 钻石会员
    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String? = null,
    @ColumnInfo(name = "avatar_frame_id")
    val avatarFrameId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
