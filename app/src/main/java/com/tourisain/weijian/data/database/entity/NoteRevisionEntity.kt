package com.tourisain.weijian.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "note_revisions",
    indices = [
        Index(value = ["note_id", "user_id"], name = "idx_note_revisions_note_user"),
        Index(value = ["user_id", "saved_at"], name = "idx_note_revisions_user_saved")
    ]
)
data class NoteRevisionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "note_id")
    val noteId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "title")
    val title: String = "",
    @ColumnInfo(name = "content")
    val content: String = "",
    @ColumnInfo(name = "color")
    val color: String = "#FFFFFF",
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,
    @ColumnInfo(name = "skin_id")
    val skinId: String = "classic",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "saved_at")
    val savedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "format")
    val format: String = "plain",
    @ColumnInfo(name = "tags")
    val tags: String? = null,
    @ColumnInfo(name = "type")
    val type: String = "text",
    @ColumnInfo(name = "attachments")
    val attachments: String? = null,
    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,
    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = false
)
