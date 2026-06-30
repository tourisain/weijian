package com.tourisain.weijian.presentation.note.detail

import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.abs

internal const val RICH_RENDER_SETTLE_MS = 320L
internal const val MAX_RICH_RENDER_LENGTH = 18_000
internal const val MAX_RICH_SELECTION_RENDER_LENGTH = 2_500
internal const val LARGE_TEXT_SYNC_DEFER_LENGTH = 24_000
internal const val LARGE_TEXT_SYNC_IMMEDIATE_DELTA = 128
internal const val ULTRA_LONG_TEXT_SYNC_DEFER_LENGTH = 80_000
internal const val ULTRA_LONG_TEXT_SYNC_IMMEDIATE_DELTA = 1_024
private const val NORMAL_SYNC_DELAY_MS = 220L
private const val ULTRA_LONG_SYNC_DELAY_MS = 420L

internal enum class EditorInteractionState {
    Idle,
    Typing,
    Selecting,
    Formatting,
    Recovering
}

internal fun editorInteractionState(
    value: TextFieldValue,
    nowMs: Long,
    lastInputAtMs: Long,
    isFormatting: Boolean = false,
    isRecovering: Boolean = false
): EditorInteractionState {
    if (isRecovering) return EditorInteractionState.Recovering
    if (isFormatting) return EditorInteractionState.Formatting
    if (abs(value.selection.end - value.selection.start) > 0) return EditorInteractionState.Selecting
    return if (nowMs - lastInputAtMs < RICH_RENDER_SETTLE_MS) {
        EditorInteractionState.Typing
    } else {
        EditorInteractionState.Idle
    }
}

internal fun shouldApplyTypingAssist(state: EditorInteractionState): Boolean {
    return state == EditorInteractionState.Idle || state == EditorInteractionState.Typing
}

internal fun shouldCommitEditorTransaction(
    previous: TextFieldValue,
    next: TextFieldValue
): Boolean {
    return previous.text != next.text ||
        previous.selection != next.selection ||
        previous.composition != next.composition
}

internal fun shouldSyncEditorText(
    previousText: String,
    nextText: String
): Boolean {
    return previousText != nextText
}

internal fun shouldUseRichRendering(
    value: TextFieldValue,
    hasRichMarker: Boolean,
    nowMs: Long,
    lastInputAtMs: Long,
    interactionState: EditorInteractionState = editorInteractionState(
        value = value,
        nowMs = nowMs,
        lastInputAtMs = lastInputAtMs
    )
): Boolean {
    if (!hasRichMarker) return false
    if (interactionState != EditorInteractionState.Idle) return false
    if (value.text.length > MAX_RICH_RENDER_LENGTH) return false
    if (value.composition != null) return false
    val selectedLength = abs(value.selection.end - value.selection.start)
    if (selectedLength > MAX_RICH_SELECTION_RENDER_LENGTH) return false
    return true
}

internal fun shouldDeferEditorSync(
    previousTextLength: Int,
    nextTextLength: Int,
    selectionLength: Int,
    hasComposition: Boolean
): Boolean {
    if (hasComposition) return true
    if (selectionLength > MAX_RICH_SELECTION_RENDER_LENGTH) return true
    val textLength = maxOf(previousTextLength, nextTextLength)
    if (textLength < LARGE_TEXT_SYNC_DEFER_LENGTH) return false
    val delta = abs(nextTextLength - previousTextLength)
    if (textLength >= ULTRA_LONG_TEXT_SYNC_DEFER_LENGTH) {
        return delta in 0 until ULTRA_LONG_TEXT_SYNC_IMMEDIATE_DELTA
    }
    return delta in 0 until LARGE_TEXT_SYNC_IMMEDIATE_DELTA
}

internal fun editorSyncDelayMs(textLength: Int): Long {
    return if (textLength >= ULTRA_LONG_TEXT_SYNC_DEFER_LENGTH) {
        ULTRA_LONG_SYNC_DELAY_MS
    } else {
        NORMAL_SYNC_DELAY_MS
    }
}
