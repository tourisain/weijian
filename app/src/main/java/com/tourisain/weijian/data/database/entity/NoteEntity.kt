package com.tourisain.weijian.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["user_id"], name = "idx_notes_user_id"),
        Index(value = ["user_id", "is_deleted"], name = "idx_notes_user_deleted"),
        Index(value = ["user_id", "created_at"], name = "idx_notes_user_created"),
        Index(value = ["user_id", "is_deleted", "created_at"], name = "idx_notes_user_deleted_created"),
        Index(value = ["user_id", "is_pinned", "created_at"], name = "idx_notes_user_pinned_created"),
        Index(value = ["category_id"], name = "idx_notes_category_id"),
        Index(value = ["user_id", "is_locked"], name = "idx_notes_user_locked"),
        Index(value = ["is_deleted", "deleted_at"], name = "idx_notes_deleted_time")
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "title")
    val title: String = "",
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "color")
    val color: String,
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,
    @ColumnInfo(name = "skin_id", defaultValue = "classic")
    val skinId: String = "classic",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "format")
    val format: String = "plain", // "plain" or "rich"
    @ColumnInfo(name = "tags")
    val tags: String? = null,
    @ColumnInfo(name = "type")
    val type: String = "text", // "text", "voice", "handwriting"
    @ColumnInfo(name = "attachments")
    val attachments: String? = null,
    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,
    @ColumnInfo(name = "is_locked", defaultValue = "0")
    val isLocked: Boolean = false,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)
