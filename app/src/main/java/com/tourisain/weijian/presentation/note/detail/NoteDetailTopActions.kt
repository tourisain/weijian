package com.tourisain.weijian.presentation.note.detail

enum class NoteDetailTopAction {
    Pin,
    Lock,
    History,
    Share,
    Export
}

data class NoteDetailTopActionItem(
    val action: NoteDetailTopAction,
    val label: String
)

fun noteDetailTopActions(isPinned: Boolean, isLocked: Boolean): List<NoteDetailTopActionItem> {
    return listOf(
        NoteDetailTopActionItem(NoteDetailTopAction.Pin, if (isPinned) "取消" else "置顶"),
        NoteDetailTopActionItem(NoteDetailTopAction.Lock, if (isLocked) "解锁" else "加锁"),
        NoteDetailTopActionItem(NoteDetailTopAction.History, "历史"),
        NoteDetailTopActionItem(NoteDetailTopAction.Share, "分享"),
        NoteDetailTopActionItem(NoteDetailTopAction.Export, "导出")
    )
}
