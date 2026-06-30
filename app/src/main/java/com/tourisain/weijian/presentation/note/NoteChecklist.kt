package com.tourisain.weijian.presentation.note

private val checklistMarker = Regex("""^\s*-\s\[[ xX]\]\s+.+""")

internal fun hasChecklistMarker(content: String): Boolean {
    if (content.isBlank()) return false
    return content.lineSequence().any { line -> checklistMarker.matches(line) }
}
