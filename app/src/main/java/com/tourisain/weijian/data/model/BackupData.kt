package com.tourisain.weijian.data.model

import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.database.entity.CategoryEntity
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.data.database.entity.NoteRevisionEntity
import com.tourisain.weijian.data.database.entity.UserEntity

data class BackupData(
    val version: Int? = 10,
    val timestamp: Long? = System.currentTimeMillis(),
    val notes: List<NoteEntity>? = null,
    val noteRevisions: List<NoteRevisionEntity>? = null,
    val accountRecords: List<AccountRecordEntity>? = null,
    val user: UserEntity? = null,
    val categories: List<CategoryEntity>? = null,
    val noteCategories: List<NoteCategoryEntity>? = null,
    val appSettings: Map<String, Any>? = null,
    val cardVisibility: Map<String, Boolean>? = null
)
