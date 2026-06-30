package com.tourisain.weijian.presentation.note.list

internal fun noteRowMetaText(
    createdTime: String,
    folderName: String?
): String {
    val cleanFolderName = folderName?.trim().orEmpty()
    return if (cleanFolderName.isBlank()) createdTime else "$createdTime \u00b7 $cleanFolderName"
}
