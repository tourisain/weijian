package com.tourisain.weijian.presentation.note.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun AddTagDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_tag)) },
        text = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppleNotesStyle.SearchSurface,
                shape = AppleNotesStyle.SearchShape
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = AppleNotesStyle.PrimaryText),
                    cursorBrush = SolidColor(AppleNotesStyle.Accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isBlank()) {
                                Text(
                                    text = stringResource(R.string.tag_name),
                                    color = AppleNotesStyle.TertiaryText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(onClick = onConfirm, enabled = value.trim().trimStart('#').isNotBlank()) {
                    Text(stringResource(R.string.add), color = AppleNotesStyle.Accent)
                }
            }
        }
    )
}

@Composable
internal fun NoteFolderPickerDialog(
    folders: List<NoteFolderOption>,
    selectedFolderId: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.move_to_folder)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FolderPickerRow(
                    title = stringResource(R.string.uncategorized),
                    color = AppleNotesStyle.SecondaryText,
                    selected = selectedFolderId == null,
                    onClick = { onSelect(null) }
                )
                folders.forEach { folder ->
                    FolderPickerRow(
                        title = folder.name,
                        color = parseFolderColor(folder.color),
                        selected = selectedFolderId == folder.id,
                        onClick = { onSelect(folder.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun FolderPickerRow(
    title: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(color = color.copy(alpha = 0.14f), shape = CircleShape) {
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                Icon(Lucide.Folder, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = AppleNotesStyle.PrimaryText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (selected) {
            Icon(Lucide.Check, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun EditorToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    contentFontSize: Int,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onHeading: () -> Unit,
    onChecklist: () -> Unit,
    onList: () -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onStrikethrough: () -> Unit,
    onQuote: () -> Unit,
    onCode: () -> Unit,
    onDivider: () -> Unit,
    onImage: () -> Unit,
    onLink: () -> Unit,
    onTable: () -> Unit,
    onAttachment: () -> Unit,
    onDelete: () -> Unit,
    onHideKeyboard: () -> Unit,
    formatToolsExpanded: Boolean,
    onFormatToolsExpandedChange: (Boolean) -> Unit
) {
    val primaryScrollState = rememberScrollState()
    val formatScrollState = rememberScrollState()
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset {
                IntOffset(
                    x = 0,
                    y = editorToolbarImeOffsetPx(imeBottomPx)
                )
            },
        color = AppleNotesStyle.Surface.copy(alpha = 0.98f),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = EDITOR_TOOLBAR_VERTICAL_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(EDITOR_TOOLBAR_LAYER_GAP_DP.dp)
        ) {
            if (formatToolsExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(formatScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    editorToolbarFormatGroups().forEach { group ->
                        ToolbarGroupLabel(group.label)
                        group.actions.forEach { item ->
                            when (item.action) {
                                EditorToolbarFormatAction.Checklist -> ToolbarButton(icon = Lucide.Check, onClick = onChecklist, label = item.label)
                                EditorToolbarFormatAction.Heading -> ToolbarButton(icon = Lucide.Type, onClick = onHeading, label = item.label)
                                EditorToolbarFormatAction.List -> ToolbarButton(icon = Lucide.List, onClick = onList, label = item.label)
                                EditorToolbarFormatAction.Bold -> ToolbarButton(icon = Lucide.Bold, onClick = onBold, label = item.label)
                                EditorToolbarFormatAction.Italic -> ToolbarButton(icon = Lucide.Italic, onClick = onItalic, label = item.label)
                                EditorToolbarFormatAction.Strikethrough -> ToolbarButton(icon = Lucide.Strikethrough, onClick = onStrikethrough, label = item.label)
                                EditorToolbarFormatAction.Quote -> ToolbarButton(icon = Lucide.AlignLeft, onClick = onQuote, label = item.label)
                                EditorToolbarFormatAction.Code -> ToolbarButton(icon = Lucide.Github, onClick = onCode, label = item.label)
                                EditorToolbarFormatAction.Divider -> ToolbarButton(icon = Lucide.Minus, onClick = onDivider, label = item.label)
                                EditorToolbarFormatAction.Image -> ToolbarButton(icon = Lucide.Image, onClick = onImage, label = item.label)
                                EditorToolbarFormatAction.Link -> ToolbarButton(icon = Lucide.Link, onClick = onLink, label = item.label)
                                EditorToolbarFormatAction.Table -> ToolbarButton(icon = Lucide.Table, onClick = onTable, label = item.label)
                                EditorToolbarFormatAction.Attachment -> ToolbarButton(icon = Lucide.FilePlus, onClick = onAttachment, label = item.label)
                                EditorToolbarFormatAction.Delete -> ToolbarButton(icon = Lucide.Trash2, onClick = onDelete, label = item.label, destructive = true)
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(primaryScrollState),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                editorToolbarPrimaryActions().forEach { item ->
                    when (item.action) {
                        EditorToolbarPrimaryAction.Undo -> {
                            ToolbarButton(icon = Lucide.Undo, onClick = onUndo, label = item.label, enabled = canUndo)
                        }
                        EditorToolbarPrimaryAction.Redo -> {
                            ToolbarButton(icon = Lucide.Redo, onClick = onRedo, label = item.label, enabled = canRedo)
                        }
                        EditorToolbarPrimaryAction.FontDecrease -> {
                            ToolbarButton(
                                icon = Lucide.Minus,
                                onClick = onDecreaseFontSize,
                                label = item.label,
                                enabled = contentFontSize > MIN_CONTENT_FONT_SIZE
                            )
                            FontSizeChip(contentFontSize)
                        }
                        EditorToolbarPrimaryAction.FontIncrease -> {
                            ToolbarButton(
                                icon = Lucide.Plus,
                                onClick = onIncreaseFontSize,
                                label = item.label,
                                enabled = contentFontSize < MAX_CONTENT_FONT_SIZE
                            )
                        }
                        EditorToolbarPrimaryAction.Format -> {
                            ToolbarButton(
                                icon = Lucide.Type,
                                onClick = { onFormatToolsExpandedChange(!formatToolsExpanded) },
                                label = item.label,
                                selected = formatToolsExpanded
                            )
                        }
                        EditorToolbarPrimaryAction.HideKeyboard -> {
                            ToolbarButton(icon = Lucide.ChevronDown, onClick = onHideKeyboard, label = item.label)
                        }
                    }
                }
            }
        }
    }
}

internal const val EDITOR_TOOLBAR_VERTICAL_PADDING_DP = 4

@Composable
private fun ToolbarGroupLabel(label: String) {
    Surface(color = Color.Transparent, modifier = Modifier.height(EDITOR_TOOLBAR_BUTTON_SIZE_DP.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = AppleNotesStyle.TertiaryText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FontSizeChip(contentFontSize: Int) {
    Surface(color = AppleNotesStyle.SearchSurface, shape = CircleShape, modifier = Modifier.size(EDITOR_TOOLBAR_BUTTON_SIZE_DP.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = contentFontSize.toString(),
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    label: String,
    enabled: Boolean = true,
    destructive: Boolean = false,
    selected: Boolean = false
) {
    val contentColor = when {
        !enabled -> AppleNotesStyle.TertiaryText
        destructive -> AppleNotesStyle.Destructive
        selected -> AppleNotesStyle.Accent
        else -> AppleNotesStyle.PrimaryText
    }
    val backgroundColor = if (selected) AppleNotesStyle.AccentSoft else AppleNotesStyle.SearchSurface
    Surface(color = backgroundColor, shape = CircleShape) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(EDITOR_TOOLBAR_BUTTON_SIZE_DP.dp)) {
            Icon(
                icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

internal enum class EditorAction {
    Heading,
    Checklist,
    List,
    Bold,
    Italic,
    Strikethrough,
    Quote,
    Code,
    Divider,
    Link,
    Table
}

internal fun applyEditorAction(value: TextFieldValue, action: EditorAction): TextFieldValue {
    return when (action) {
        EditorAction.Heading -> toggleHeadingLevel(value)
        EditorAction.Checklist -> toggleChecklistMarker(value)
        EditorAction.List -> toggleLineMarker(value, LineMarker.Bullet)
        EditorAction.Bold -> toggleInlineMarker(value, InlineMarker.Bold)
        EditorAction.Italic -> toggleInlineMarker(value, InlineMarker.Italic)
        EditorAction.Strikethrough -> toggleInlineMarker(value, InlineMarker.Strikethrough)
        EditorAction.Quote -> toggleLineMarker(value, LineMarker.Quote)
        EditorAction.Code -> toggleCode(value)
        EditorAction.Divider -> insertAtSelection(value, dividerInsertion(value))
        EditorAction.Link -> insertAtSelection(value, linkInsertion(value))
        EditorAction.Table -> insertAtSelection(value, tableInsertion(value))
    }
}

private enum class InlineMarker(val prefix: String, val suffix: String) {
    Bold("**", "**"),
    Italic("*", "*"),
    Strikethrough("~~", "~~"),
    Code("`", "`")
}

private enum class LineMarker(val prefix: String) {
    Heading("# "),
    Checklist("- [ ] "),
    Bullet("- "),
    Quote("> ")
}

private data class TextBounds(val start: Int, val end: Int)

internal fun normalizeTitleInput(value: String): String {
    val builder = StringBuilder(minOf(value.length, MAX_TITLE_LENGTH))
    var previousWasSpace = false
    for (character in value) {
        val normalized = when (character) {
            '\u0000', '\r', '\n', '\t' -> ' '
            else -> character
        }
        if (normalized == ' ') {
            if (previousWasSpace) continue
            previousWasSpace = true
        } else {
            previousWasSpace = false
        }
        builder.append(normalized)
        if (builder.length >= MAX_TITLE_LENGTH) break
    }
    return builder.toString()
}

internal fun normalizeEditorValue(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val invalidIndex = if (text.length <= MAX_EDITOR_NORMALIZE_SCAN_LENGTH) {
        firstEditorControlCharacterIndex(text, 0, text.length)
    } else {
        val start = minOf(value.selection.start, value.selection.end)
            .coerceIn(0, text.length)
            .let { (it - LARGE_TEXT_NORMALIZE_WINDOW).coerceAtLeast(0) }
        val end = maxOf(value.selection.start, value.selection.end)
            .coerceIn(0, text.length)
            .let { (it + LARGE_TEXT_NORMALIZE_WINDOW).coerceAtMost(text.length) }
        firstEditorControlCharacterIndex(text, start, end)
    }
    if (invalidIndex == -1) return value

    var changed = false
    val offsetMap = IntArray(text.length + 1)
    val builder = StringBuilder(text.length)
    text.forEachIndexed { index, character ->
        offsetMap[index] = builder.length
        when (character) {
            '\u0000' -> changed = true
            '\r' -> {
                changed = true
                if (text.getOrNull(index + 1) != '\n') builder.append('\n')
            }
            else -> builder.append(character)
        }
    }
    offsetMap[text.length] = builder.length
    if (!changed) return value
    val normalizedText = builder.toString()
    return TextFieldValue(
        text = normalizedText,
        selection = TextRange(
            offsetMap[value.selection.start.coerceIn(0, text.length)].coerceIn(0, normalizedText.length),
            offsetMap[value.selection.end.coerceIn(0, text.length)].coerceIn(0, normalizedText.length)
        )
    )
}

internal fun applyEditorTypingAssist(current: TextFieldValue, next: TextFieldValue): TextFieldValue {
    if (current.composition != null || next.composition != null) return next
    if (current.selection.start != current.selection.end || next.selection.start != next.selection.end) return next

    val oldText = current.text
    val newText = next.text
    val insertedLength = newText.length - oldText.length
    if (insertedLength !in 1..2) return next

    val oldCursor = current.selection.start.coerceIn(0, oldText.length)
    val newCursor = next.selection.start.coerceIn(0, newText.length)
    val insertedStart = newCursor - insertedLength
    if (insertedStart != oldCursor || insertedStart < 0) return next

    val inserted = newText.substring(insertedStart, newCursor)
    if (inserted != "\n" && inserted != "\r\n") return next

    val lineBounds = currentLineBounds(oldText, oldCursor)
    val lineBeforeCursor = oldText.substring(lineBounds.start, oldCursor)
    val continuation = continuationForLine(lineBeforeCursor) ?: return next
    val fullLine = oldText.substring(lineBounds.start, lineBounds.end)
    val isEmptyMarkedLine = fullLine
        .drop(continuation.contentStartInLine)
        .isBlank()

    if (isEmptyMarkedLine) {
        val cleanedLine = lineBeforeCursor.takeWhile { it == ' ' || it == '\t' }
        val nextText = oldText.replaceRange(lineBounds.start, lineBounds.end, cleanedLine)
        val cursor = lineBounds.start + cleanedLine.length
        return TextFieldValue(nextText, TextRange(cursor))
    }

    val insertion = "\n${continuation.prefix}"
    val nextText = oldText.substring(0, oldCursor) + insertion + oldText.substring(oldCursor)
    val cursor = oldCursor + insertion.length
    return TextFieldValue(nextText, TextRange(cursor))
}

private data class LineContinuation(
    val prefix: String,
    val contentStartInLine: Int
)

private fun continuationForLine(lineBeforeCursor: String): LineContinuation? {
    val indentLength = lineBeforeCursor.indexOfFirst { it != ' ' && it != '\t' }
        .let { if (it == -1) lineBeforeCursor.length else it }
    val indent = lineBeforeCursor.take(indentLength)
    val body = lineBeforeCursor.drop(indentLength)
    return when {
        body.startsWith("- [ ] ") || body.startsWith("- [x] ", ignoreCase = true) ->
            LineContinuation(prefix = "$indent- [ ] ", contentStartInLine = indentLength + 6)
        body.startsWith("- ") && !body.startsWith("- [ ] ") && !body.startsWith("- [x] ", ignoreCase = true) ->
            LineContinuation(prefix = "$indent- ", contentStartInLine = indentLength + 2)
        body.startsWith("> ") ->
            LineContinuation(prefix = "$indent> ", contentStartInLine = indentLength + 2)
        else -> numberedLineContinuation(indent, body, indentLength)
    }
}

private fun numberedLineContinuation(indent: String, body: String, indentLength: Int): LineContinuation? {
    val match = NUMBERED_LIST_PREFIX_REGEX.find(body) ?: return null
    if (match.range.first != 0) return null
    val number = match.groupValues[1].toIntOrNull() ?: return null
    val delimiter = match.groupValues[2]
    val nextPrefix = "$indent${number + 1}$delimiter "
    return LineContinuation(prefix = nextPrefix, contentStartInLine = indentLength + match.value.length)
}

private fun firstEditorControlCharacterIndex(text: String, start: Int, end: Int): Int {
    var index = start
    while (index < end) {
        when (text[index]) {
            '\u0000', '\r' -> return index
        }
        index++
    }
    return -1
}

internal fun stableSelectionAfterContentSync(currentValue: TextFieldValue, nextText: String): TextRange {
    if (currentValue.text.isBlank() && nextText.isNotBlank()) {
        return TextRange(nextText.length)
    }
    return coerceTextRange(currentValue.selection, nextText.length)
}

private fun coerceTextRange(range: TextRange, textLength: Int): TextRange {
    return TextRange(
        range.start.coerceIn(0, textLength),
        range.end.coerceIn(0, textLength)
    )
}

private fun toggleInlineMarker(value: TextFieldValue, marker: InlineMarker): TextFieldValue {
    val text = value.text
    val bounds = selectedOrCurrentWordBounds(value)
    val selected = text.substring(bounds.start, bounds.end)

    if (selected.isBlank() && bounds.start == bounds.end) {
        val insertion = marker.prefix + marker.suffix
        val nextText = text.replaceRange(bounds.start, bounds.end, insertion)
        val cursor = bounds.start + marker.prefix.length
        return TextFieldValue(nextText, TextRange(cursor))
    }

    val insideWrapped = selectedHasOwnMarker(selected, marker)
    if (insideWrapped) {
        val replacement = selected
            .removePrefix(marker.prefix)
            .removeSuffix(marker.suffix)
        val nextText = text.replaceRange(bounds.start, bounds.end, replacement)
        return TextFieldValue(nextText, TextRange(bounds.start, bounds.start + replacement.length))
    }

    val aroundWrapped = hasMarkerAround(text, bounds.start, bounds.end, marker)
    if (aroundWrapped) {
        val nextText = buildString {
            append(text.substring(0, bounds.start - marker.prefix.length))
            append(selected)
            append(text.substring(bounds.end + marker.suffix.length))
        }
        return TextFieldValue(nextText, TextRange(bounds.start - marker.prefix.length, bounds.end - marker.prefix.length))
    }

    val replacement = marker.prefix + selected + marker.suffix
    val nextText = text.replaceRange(bounds.start, bounds.end, replacement)
    return TextFieldValue(
        nextText,
        TextRange(
            bounds.start + marker.prefix.length,
            bounds.start + marker.prefix.length + selected.length
        )
    )
}

private fun selectedOrCurrentWordBounds(value: TextFieldValue): TextBounds {
    val text = value.text
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val end = maxOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    if (start != end) return TextBounds(start, end)

    if (text.isEmpty()) return TextBounds(start, end)
    val probe = when {
        start < text.length && !text[start].isWhitespace() -> start
        start > 0 && !text[start - 1].isWhitespace() -> start - 1
        else -> return TextBounds(start, end)
    }

    var left = probe
    var right = probe + 1
    while (left > 0 && !text[left - 1].isWhitespace()) left--
    while (right < text.length && !text[right].isWhitespace()) right++
    return TextBounds(left, right)
}

private fun selectedHasOwnMarker(selected: String, marker: InlineMarker): Boolean {
    if (selected.length < marker.prefix.length + marker.suffix.length) return false
    if (!selected.startsWith(marker.prefix) || !selected.endsWith(marker.suffix)) return false
    return marker != InlineMarker.Italic || !selected.startsWith("**")
}

private fun hasMarkerAround(text: String, start: Int, end: Int, marker: InlineMarker): Boolean {
    if (start < marker.prefix.length || end + marker.suffix.length > text.length) return false
    val hasPrefix = text.substring(start - marker.prefix.length, start) == marker.prefix
    val hasSuffix = text.substring(end, end + marker.suffix.length) == marker.suffix
    if (!hasPrefix || !hasSuffix) return false
    return marker != InlineMarker.Italic || isSingleAsteriskPair(text, start, end)
}

private fun isSingleAsteriskPair(text: String, start: Int, end: Int): Boolean {
    val beforeIsDouble = start >= 2 && text.substring(start - 2, start) == "**"
    val afterIsDouble = end + 2 <= text.length && text.substring(end, end + 2) == "**"
    return !beforeIsDouble && !afterIsDouble
}

private fun toggleCode(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val end = maxOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val existingBlock = fencedCodeBlockAround(text, start, end)
    if (existingBlock != null) {
        val block = text.substring(existingBlock.start, existingBlock.end)
        val inner = block.removePrefix("```\n").removeSuffix("\n```")
        val nextText = text.replaceRange(existingBlock.start, existingBlock.end, inner)
        return TextFieldValue(nextText, TextRange(existingBlock.start, existingBlock.start + inner.length))
    }

    val blockBounds = if (start == end) currentLineBounds(text, start) else selectedLineBounds(text, start, end)
    val block = text.substring(blockBounds.start, blockBounds.end)
    val replacement = if (block.isBlank()) {
        "```\n\n```"
    } else {
        "```\n$block\n```"
    }
    val nextText = text.replaceRange(blockBounds.start, blockBounds.end, replacement)
    return if (block.isBlank()) {
        TextFieldValue(nextText, TextRange(blockBounds.start + 4))
    } else {
        TextFieldValue(nextText, TextRange(blockBounds.start + 4, blockBounds.start + 4 + block.length))
    }
}

private fun fencedCodeBlockAround(text: String, start: Int, end: Int): TextBounds? {
    if (text.isEmpty()) return null
    val openingStart = findPreviousFenceLineStart(text, start) ?: return null
    val openingEnd = lineEndIncludingNoNewline(text, openingStart)
    val closingStart = findNextFenceLineStart(text, maxOf(end, openingEnd + 1)) ?: return null
    val closingEnd = lineEndIncludingNoNewline(text, closingStart)
    if (start < openingStart || end > closingEnd) return null
    return TextBounds(openingStart, closingEnd)
}

private fun findPreviousFenceLineStart(text: String, offset: Int): Int? {
    var lineStart = currentLineBounds(text, offset).start
    while (lineStart >= 0) {
        val lineEnd = lineEndIncludingNoNewline(text, lineStart)
        if (text.substring(lineStart, lineEnd).trim() == "```") return lineStart
        if (lineStart == 0) break
        lineStart = text.lastIndexOf('\n', maxOf(0, lineStart - 2)).let { if (it == -1) 0 else it + 1 }
    }
    return null
}

private fun findNextFenceLineStart(text: String, offset: Int): Int? {
    var lineStart = currentLineBounds(text, offset.coerceIn(0, text.length)).start
    while (lineStart <= text.length) {
        val lineEnd = lineEndIncludingNoNewline(text, lineStart)
        if (text.substring(lineStart, lineEnd).trim() == "```") return lineStart
        val nextNewline = text.indexOf('\n', lineStart)
        if (nextNewline == -1) break
        lineStart = nextNewline + 1
    }
    return null
}

private fun lineEndIncludingNoNewline(text: String, lineStart: Int): Int {
    return text.indexOf('\n', lineStart).let { if (it == -1) text.length else it }
}

private fun toggleLineMarker(value: TextFieldValue, marker: LineMarker): TextFieldValue {
    val text = value.text
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val end = maxOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val lineBounds = selectedLineBounds(text, start, end)
    val segment = text.substring(lineBounds.start, lineBounds.end)
    val shouldRemove = shouldRemoveLineMarker(segment, marker)
    val transformed = transformLines(segment, marker, shouldRemove)
    val nextText = text.replaceRange(lineBounds.start, lineBounds.end, transformed)
    val nextSelection = if (start == end) {
        val delta = transformed.length - segment.length
        TextRange((start + delta).coerceIn(lineBounds.start, lineBounds.start + transformed.length))
    } else {
        TextRange(lineBounds.start, lineBounds.start + transformed.length)
    }
    return TextFieldValue(nextText, nextSelection)
}

private fun toggleHeadingLevel(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val end = maxOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val lineBounds = selectedLineBounds(text, start, end)
    val segment = text.substring(lineBounds.start, lineBounds.end)
    val transformed = transformHeadingLines(segment)
    val nextText = text.replaceRange(lineBounds.start, lineBounds.end, transformed)
    val nextSelection = if (start == end) {
        val delta = transformed.length - segment.length
        TextRange((start + delta).coerceIn(lineBounds.start, lineBounds.start + transformed.length))
    } else {
        TextRange(lineBounds.start, lineBounds.start + transformed.length)
    }
    return TextFieldValue(nextText, nextSelection)
}

private fun transformHeadingLines(segment: String): String {
    val builder = StringBuilder(segment.length + 32)
    var cursor = 0
    while (cursor <= segment.length) {
        val nextBreak = segment.indexOf('\n', cursor).let { if (it == -1) segment.length else it }
        val line = segment.substring(cursor, nextBreak)
        builder.append(transformHeadingLine(line))
        if (nextBreak == segment.length) break
        builder.append('\n')
        cursor = nextBreak + 1
    }
    return builder.toString()
}

private fun transformHeadingLine(line: String): String {
    if (line.isBlank()) return line
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    val body = line.drop(indent.length)
    val currentLevel = extractHeadingLevel(body)
    val plain = removeHeadingPrefix(body).trimStart()
    val nextLevel = if (currentLevel >= 6) 0 else currentLevel + 1
    return if (nextLevel == 0) {
        indent + plain
    } else {
        indent + "#".repeat(nextLevel) + " " + plain
    }
}

private fun extractHeadingLevel(line: String): Int {
    var count = 0
    for (char in line) {
        if (char == '#') count++
        else if (char == ' ') break
        else return 0
    }
    return if (count > 0 && line.getOrNull(count) == ' ') count else 0
}

private fun removeHeadingPrefix(line: String): String {
    val level = extractHeadingLevel(line)
    return if (level > 0) line.drop(level + 1) else line
}

private fun toggleChecklistMarker(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val end = maxOf(value.selection.start, value.selection.end).coerceIn(0, text.length)
    val lineBounds = selectedLineBounds(text, start, end)
    val segment = text.substring(lineBounds.start, lineBounds.end)
    val action = checklistActionFor(segment)
    val transformed = transformChecklistLines(segment, action)
    val nextText = text.replaceRange(lineBounds.start, lineBounds.end, transformed)
    val nextSelection = if (start == end) {
        val delta = transformed.length - segment.length
        TextRange((start + delta).coerceIn(lineBounds.start, lineBounds.start + transformed.length))
    } else {
        TextRange(lineBounds.start, lineBounds.start + transformed.length)
    }
    return TextFieldValue(nextText, nextSelection)
}

internal fun toggleChecklistAtCursor(value: TextFieldValue): TextFieldValue {
    val text = value.text
    if (text.isEmpty()) return value
    val cursor = value.selection.start.coerceIn(0, text.length)
    val bounds = currentLineBounds(text, cursor)
    val line = text.substring(bounds.start, bounds.end)
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    val body = line.drop(indent.length)
    val nextBody = when {
        body.startsWith("- [ ] ") -> "- [x] ${body.removePrefix("- [ ] ")}"
        body.startsWith("- [x] ", ignoreCase = true) -> "- [ ] ${body.drop(6)}"
        else -> return value
    }
    val replacement = indent + nextBody
    val nextText = text.replaceRange(bounds.start, bounds.end, replacement)
    return TextFieldValue(nextText, value.selection)
}

private enum class ChecklistAction {
    AddUnchecked,
    MarkChecked,
    Remove
}

private fun checklistActionFor(segment: String): ChecklistAction {
    var hasMeaningfulLine = false
    var allChecklist = true
    var allChecked = true

    forEachLine(segment) { line ->
        if (line.isNotBlank()) {
            hasMeaningfulLine = true
            val body = line.trimStart()
            val checked = body.startsWith("- [x] ", ignoreCase = true)
            val unchecked = body.startsWith("- [ ] ")
            allChecklist = allChecklist && (checked || unchecked)
            allChecked = allChecked && checked
        }
    }

    return when {
        !hasMeaningfulLine -> ChecklistAction.AddUnchecked
        !allChecklist -> ChecklistAction.AddUnchecked
        allChecked -> ChecklistAction.Remove
        else -> ChecklistAction.MarkChecked
    }
}

private fun transformChecklistLines(segment: String, action: ChecklistAction): String {
    val builder = StringBuilder(segment.length + 32)
    var cursor = 0
    while (cursor <= segment.length) {
        val nextBreak = segment.indexOf('\n', cursor).let { if (it == -1) segment.length else it }
        val line = segment.substring(cursor, nextBreak)
        builder.append(transformChecklistLine(line, action))
        if (nextBreak == segment.length) break
        builder.append('\n')
        cursor = nextBreak + 1
    }
    return builder.toString()
}

private fun transformChecklistLine(line: String, action: ChecklistAction): String {
    if (line.isBlank()) return line
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    val body = line.drop(indent.length)
    val plain = removeKnownLineMarkerBody(body).trimStart()
    return when (action) {
        ChecklistAction.AddUnchecked -> "$indent- [ ] $plain"
        ChecklistAction.MarkChecked -> "$indent- [x] $plain"
        ChecklistAction.Remove -> indent + body
            .removePrefix("- [ ] ")
            .removePrefix("- [x] ")
            .removePrefix("- [X] ")
    }
}

private inline fun forEachLine(segment: String, block: (String) -> Unit) {
    var cursor = 0
    while (cursor <= segment.length) {
        val nextBreak = segment.indexOf('\n', cursor).let { if (it == -1) segment.length else it }
        block(segment.substring(cursor, nextBreak))
        if (nextBreak == segment.length) break
        cursor = nextBreak + 1
    }
}

private fun shouldRemoveLineMarker(segment: String, marker: LineMarker): Boolean {
    var hasMeaningfulLine = false
    var cursor = 0
    while (cursor <= segment.length) {
        val nextBreak = segment.indexOf('\n', cursor).let { if (it == -1) segment.length else it }
        val line = segment.substring(cursor, nextBreak)
        if (line.isNotBlank()) {
            hasMeaningfulLine = true
            if (!hasLineMarker(line, marker)) return false
        }
        if (nextBreak == segment.length) break
        cursor = nextBreak + 1
    }
    return hasMeaningfulLine
}

private fun transformLines(segment: String, marker: LineMarker, remove: Boolean): String {
    val builder = StringBuilder(segment.length + 32)
    var cursor = 0
    while (cursor <= segment.length) {
        val nextBreak = segment.indexOf('\n', cursor).let { if (it == -1) segment.length else it }
        val line = segment.substring(cursor, nextBreak)
        if (remove) {
            builder.append(removeLineMarker(line, marker))
        } else {
            builder.append(addLineMarker(line, marker))
        }
        if (nextBreak == segment.length) break
        builder.append('\n')
        cursor = nextBreak + 1
    }
    return builder.toString()
}

private fun addLineMarker(line: String, marker: LineMarker): String {
    if (line.isBlank()) return line
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    val body = line.drop(indent.length)
    return indent + marker.prefix + removeKnownLineMarkerBody(body).trimStart()
}

private fun selectedLineBounds(text: String, start: Int, end: Int): TextBounds {
    if (text.isEmpty()) return TextBounds(0, 0)
    val safeStart = start.coerceIn(0, text.length)
    val safeEnd = end.coerceIn(0, text.length)
    val lineStart = if (safeStart == 0) 0 else text.lastIndexOf('\n', safeStart - 1).let { if (it == -1) 0 else it + 1 }
    val endProbe = when {
        safeEnd > safeStart && safeEnd <= text.length && text.getOrNull(safeEnd - 1) == '\n' -> safeEnd - 1
        safeEnd == text.length -> safeEnd
        else -> safeEnd
    }
    val lineEnd = text.indexOf('\n', endProbe).let { if (it == -1) text.length else it }
    return TextBounds(lineStart, lineEnd)
}

private fun currentLineBounds(text: String, cursor: Int): TextBounds {
    val safeCursor = cursor.coerceIn(0, text.length)
    val lineStart = if (safeCursor == 0) 0 else text.lastIndexOf('\n', safeCursor - 1).let { if (it == -1) 0 else it + 1 }
    val lineEnd = text.indexOf('\n', safeCursor).let { if (it == -1) text.length else it }
    return TextBounds(lineStart, lineEnd)
}

private fun isFencedCodeBlock(text: String): Boolean {
    return text.startsWith("```\n") && text.endsWith("\n```") && text.length >= 7
}

private fun hasLineMarker(line: String, marker: LineMarker): Boolean {
    val body = line.trimStart()
    return when (marker) {
        LineMarker.Checklist -> body.startsWith("- [ ] ") || body.startsWith("- [x] ", ignoreCase = true)
        LineMarker.Bullet -> body.startsWith("- ") && !body.startsWith("- [ ] ") && !body.startsWith("- [x] ", ignoreCase = true)
        else -> body.startsWith(marker.prefix)
    }
}

private fun removeLineMarker(line: String, marker: LineMarker): String {
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    val body = line.drop(indent.length)
    val cleaned = when (marker) {
        LineMarker.Checklist -> body
            .removePrefix("- [ ] ")
            .removePrefix("- [x] ")
            .removePrefix("- [X] ")
        else -> body.removePrefix(marker.prefix)
    }
    return indent + cleaned
}

private fun removeKnownLineMarker(line: String): String {
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    val body = line.drop(indent.length)
    return indent + removeKnownLineMarkerBody(body)
}

private fun removeKnownLineMarkerBody(body: String): String {
    return body
        .removePrefix("# ")
        .removePrefix("> ")
        .removePrefix("- [ ] ")
        .removePrefix("- [x] ")
        .removePrefix("- [X] ")
        .removePrefix("- ")
}

internal fun insertAtSelection(value: TextFieldValue, insertion: String): TextFieldValue {
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, value.text.length)
    val end = maxOf(value.selection.start, value.selection.end).coerceIn(0, value.text.length)
    val nextText = value.text.replaceRange(start, end, insertion)
    val cursor = dividerCursorOffset(start, insertion, start + insertion.length)
    return TextFieldValue(nextText, TextRange(cursor.coerceIn(0, nextText.length)))
}

private fun dividerInsertion(value: TextFieldValue): String {
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, value.text.length)
    val needsLeadingBreak = start > 0 && value.text.getOrNull(start - 1) != '\n'
    val needsTrailingBreak = value.text.getOrNull(start) != '\n'
    return buildString {
        if (needsLeadingBreak) append("\n")
        append("---")
        if (needsTrailingBreak) append("\n")
    }
}

private fun linkInsertion(value: TextFieldValue): String {
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, value.text.length)
    val end = maxOf(value.selection.start, value.selection.end).coerceIn(0, value.text.length)
    val selectedText = value.text.substring(start, end)
    val displayText = selectedText.ifBlank { "链接文本" }
    return "[$displayText](https://)"
}

private fun tableInsertion(value: TextFieldValue): String {
    val start = minOf(value.selection.start, value.selection.end).coerceIn(0, value.text.length)
    val needsLeadingBreak = start > 0 && value.text.getOrNull(start - 1) != '\n'
    return buildString {
        if (needsLeadingBreak) append("\n")
        append("| 列1 | 列2 | 列3 |\n")
        append("| --- | --- | --- |\n")
        append("| 内容 | 内容 | 内容 |\n")
    }
}

private fun dividerCursorOffset(start: Int, insertion: String, fallback: Int): Int {
    val dividerIndex = insertion.indexOf("---")
    if (dividerIndex == -1) return fallback
    val afterDivider = dividerIndex + 3
    return if (insertion.getOrNull(afterDivider) == '\n') start + afterDivider + 1 else fallback
}

fun formatSaveTime(timestamp: Long): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

internal fun formatRevisionTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

internal fun parseFolderColor(value: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(value)) }
        .getOrDefault(AppleNotesStyle.Accent)
}

internal fun attachmentDisplayName(value: String): String {
    val uri = runCatching { Uri.parse(value) }.getOrNull()
    return uri?.lastPathSegment
        ?.substringAfterLast('/')
        ?.let(Uri::decode)
        ?.takeIf { it.isNotBlank() }
        ?: value.takeLast(48)
}

internal fun openAttachment(context: Context, value: String) {
    val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, null)) }
}

internal fun writeNoteExport(
    context: Context,
    uri: Uri,
    title: String,
    content: String,
    tags: List<String>,
    attachments: List<String>
) {
    val body = buildNoteMarkdown(title, content, tags, attachments)
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.bufferedWriter(Charsets.UTF_8).use { writer -> writer.write(body) }
        }
    }
}

internal fun shareNoteMarkdown(
    context: Context,
    title: String,
    content: String,
    tags: List<String>,
    attachments: List<String>
) {
    val body = buildNoteMarkdown(title, content, tags, attachments)
    val exportTitle = title.ifBlank { "Untitled" }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/markdown"
        putExtra(Intent.EXTRA_SUBJECT, exportTitle)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_content)))
    }
}

private fun buildNoteMarkdown(
    title: String,
    content: String,
    tags: List<String>,
    attachments: List<String>
): String {
    val exportTitle = title.ifBlank { "Untitled" }
    return buildString {
        appendLine("# $exportTitle")
        if (tags.isNotEmpty()) {
            appendLine()
            appendLine(tags.joinToString(" ") { "#$it" })
        }
        appendLine()
        appendLine(content)
        if (attachments.isNotEmpty()) {
            appendLine()
            appendLine("## Attachments")
            attachments.forEach { attachment ->
                appendLine("- ${attachmentDisplayName(attachment)}")
            }
        }
    }
}

internal fun noteExportFileName(title: String): String {
    val clean = title
        .ifBlank { "weijian-note" }
        .replace(Regex("[\\\\/:*?\"<>|\\r\\n\\t]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(48)
        .ifBlank { "weijian-note" }
    return "$clean.md"
}

internal const val MIN_CONTENT_FONT_SIZE = 15
internal const val DEFAULT_CONTENT_FONT_SIZE = 18
internal const val MAX_CONTENT_FONT_SIZE = 24
private const val MAX_TITLE_LENGTH = 120
private const val MAX_EDITOR_NORMALIZE_SCAN_LENGTH = 12000
private const val LARGE_TEXT_NORMALIZE_WINDOW = 96
private val NUMBERED_LIST_PREFIX_REGEX = Regex("""(\d{1,4})([.)])\s+""")
