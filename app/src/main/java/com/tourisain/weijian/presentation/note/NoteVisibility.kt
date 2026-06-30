package com.tourisain.weijian.presentation.note

import com.tourisain.weijian.data.database.entity.NoteEntity

internal fun NoteEntity.isVisibleStandaloneNote(): Boolean {
    if (isArchived) return false
    if (type.equals("diary", ignoreCase = true)) return false
    if (id.startsWith("diary_", ignoreCase = true)) return false
    return true
}
