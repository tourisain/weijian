package com.tourisain.weijian.presentation.note.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.sp
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

internal class RichNoteVisualTransformation : VisualTransformation {
    private var cachedSource: String? = null
    private var cachedResult: TransformedText? = null

    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.length > MAX_RICH_RENDER_LENGTH) {
            cachedSource = null
            cachedResult = null
            return TransformedText(text, OffsetMapping.Identity)
        }
        if (cachedSource == text.text) {
            cachedResult?.let { return it }
        }
        return runCatching {
            RichNoteRenderer(text.text).render()
        }.getOrElse {
            TransformedText(text, OffsetMapping.Identity)
        }.also { transformed ->
            cachedSource = text.text
            cachedResult = transformed
        }
    }
}

internal class RichNoteRenderer(private val source: String) {
    private val output = AnnotatedString.Builder()
    private val originalToTransformed = IntArray(source.length + 1) { UNSET_OFFSET }
    private val transformedToOriginal = mutableListOf<Int>()

    fun render(): TransformedText {
        var lineStart = 0
        var insideCodeBlock = false

        while (lineStart <= source.length) {
            val newlineIndex = source.indexOf('\n', lineStart)
            val lineEnd = if (newlineIndex == -1) source.length else newlineIndex
            val hasNewline = newlineIndex != -1
            val line = source.substring(lineStart, lineEnd)
            val trimmedStart = line.indexOfFirst { !it.isWhitespace() }.let { if (it == -1) line.length else it }
            val markerStart = lineStart + trimmedStart
            val isFence = source.startsWith(CODE_FENCE, markerStart)

            when {
                isFence -> {
                    hide(lineStart, lineEnd)
                    insideCodeBlock = !insideCodeBlock
                }
                insideCodeBlock -> appendSourceRange(
                    start = lineStart,
                    end = lineEnd,
                    style = CODE_BLOCK_STYLE,
                    emptyPlaceholder = CODE_BLOCK_PLACEHOLDER
                )
                else -> appendRichLine(lineStart, lineEnd)
            }

            if (hasNewline) {
                if (!isFence) {
                    appendSourceChar(lineEnd)
                } else {
                    hide(lineEnd, lineEnd + 1)
                }
                lineStart = lineEnd + 1
            } else {
                break
            }
        }

        finishOffsets()
        return TransformedText(
            text = output.toAnnotatedString(),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return originalToTransformed[offset.coerceIn(0, source.length)]
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 0) return 0
                    if (offset >= transformedToOriginal.size) return source.length
                    return transformedToOriginal[offset].coerceIn(0, source.length)
                }
            }
        )
    }

    private fun appendRichLine(lineStart: Int, lineEnd: Int) {
        val marker = detectLineDisplayMarker(lineStart, lineEnd)
        if (marker == null) {
            appendInlineRange(lineStart, lineEnd, SpanStyle())
            return
        }

        appendSourceRange(lineStart, marker.prefixStart, SpanStyle())
        hide(marker.prefixStart, marker.contentStart)
        if (marker.visualPrefix.isNotEmpty()) {
            appendVisual(marker.prefixStart, marker.visualPrefix, marker.prefixStyle)
        }
        appendInlineRange(marker.contentStart, lineEnd, marker.contentStyle)
    }

    private fun detectLineDisplayMarker(lineStart: Int, lineEnd: Int): DisplayLineMarker? {
        val indentEnd = firstNonWhitespace(lineStart, lineEnd)
        return when {
            source.startsWith("# ", indentEnd) -> DisplayLineMarker(
                prefixStart = indentEnd,
                contentStart = indentEnd + 2,
                visualPrefix = "",
                prefixStyle = SpanStyle(),
                contentStyle = HEADING_STYLE
            )
            source.startsWith("- [ ] ", indentEnd) -> DisplayLineMarker(
                prefixStart = indentEnd,
                contentStart = indentEnd + 6,
                visualPrefix = TODO_UNCHECKED,
                prefixStyle = TODO_STYLE,
                contentStyle = SpanStyle()
            )
            source.startsWith("- [x] ", indentEnd, ignoreCase = true) -> DisplayLineMarker(
                prefixStart = indentEnd,
                contentStart = indentEnd + 6,
                visualPrefix = TODO_CHECKED,
                prefixStyle = TODO_STYLE,
                contentStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
            )
            source.startsWith("- ", indentEnd) -> DisplayLineMarker(
                prefixStart = indentEnd,
                contentStart = indentEnd + 2,
                visualPrefix = BULLET_PREFIX,
                prefixStyle = LIST_STYLE,
                contentStyle = SpanStyle()
            )
            source.startsWith("> ", indentEnd) -> DisplayLineMarker(
                prefixStart = indentEnd,
                contentStart = indentEnd + 2,
                visualPrefix = QUOTE_PREFIX,
                prefixStyle = QUOTE_BAR_STYLE,
                contentStyle = QUOTE_TEXT_STYLE
            )
            else -> null
        }
    }

    private fun appendInlineRange(start: Int, end: Int, baseStyle: SpanStyle) {
        var cursor = start
        while (cursor < end) {
            when {
                source.startsWith("**", cursor) -> {
                    val close = source.indexOf("**", cursor + 2)
                    if (close in (cursor + 2) until end) {
                        hide(cursor, cursor + 2)
                        appendInlineRange(cursor + 2, close, baseStyle.merge(SpanStyle(fontWeight = FontWeight.Bold)))
                        hide(close, close + 2)
                        cursor = close + 2
                    } else {
                        appendSourceChar(cursor, baseStyle)
                        cursor += 1
                    }
                }
                source.startsWith("~~", cursor) -> {
                    val close = source.indexOf("~~", cursor + 2)
                    if (close in (cursor + 2) until end) {
                        hide(cursor, cursor + 2)
                        appendInlineRange(cursor + 2, close, baseStyle.merge(SpanStyle(textDecoration = TextDecoration.LineThrough)))
                        hide(close, close + 2)
                        cursor = close + 2
                    } else {
                        appendSourceChar(cursor, baseStyle)
                        cursor += 1
                    }
                }
                source[cursor] == '`' -> {
                    val close = source.indexOf('`', cursor + 1)
                    if (close in (cursor + 1) until end) {
                        hide(cursor, cursor + 1)
                        appendSourceRange(cursor + 1, close, baseStyle.merge(INLINE_CODE_STYLE))
                        hide(close, close + 1)
                        cursor = close + 1
                    } else {
                        appendSourceChar(cursor, baseStyle)
                        cursor += 1
                    }
                }
                source[cursor] == '*' && !source.startsWith("**", cursor) -> {
                    val close = findItalicClose(cursor + 1, end)
                    if (close != -1) {
                        hide(cursor, cursor + 1)
                        appendInlineRange(cursor + 1, close, baseStyle.merge(ITALIC_STYLE))
                        hide(close, close + 1)
                        cursor = close + 1
                    } else {
                        appendSourceChar(cursor, baseStyle)
                        cursor += 1
                    }
                }
                else -> {
                    appendSourceChar(cursor, baseStyle)
                    cursor += 1
                }
            }
        }
    }

    private fun findItalicClose(start: Int, end: Int): Int {
        var cursor = start
        while (cursor < end) {
            if (source[cursor] == '*' && !source.startsWith("**", cursor)) return cursor
            cursor += 1
        }
        return -1
    }

    private fun firstNonWhitespace(start: Int, end: Int): Int {
        var cursor = start
        while (cursor < end && source[cursor].isWhitespace() && source[cursor] != '\n') {
            cursor += 1
        }
        return cursor
    }

    private fun hide(start: Int, end: Int) {
        val transformed = output.length
        for (index in start until end.coerceAtMost(source.length)) {
            originalToTransformed[index] = transformed
        }
        originalToTransformed[end.coerceIn(0, source.length)] = transformed
    }

    private fun appendVisual(sourceIndex: Int, text: String, style: SpanStyle) {
        output.pushStyle(style)
        text.forEach { char ->
            transformedToOriginal += sourceIndex.coerceIn(0, source.length)
            output.append(char)
        }
        output.pop()
    }

    private fun appendSourceRange(
        start: Int,
        end: Int,
        style: SpanStyle,
        emptyPlaceholder: String = ""
    ) {
        if (start == end && emptyPlaceholder.isNotEmpty()) {
            appendVisual(start, emptyPlaceholder, style.merge(PLACEHOLDER_STYLE))
            return
        }
        if (style == CODE_BLOCK_STYLE) {
            appendVisual(start, CODE_BLOCK_INDENT, style)
        }
        for (index in start until end) {
            appendSourceChar(index, style)
        }
    }

    private fun appendSourceChar(index: Int, style: SpanStyle = SpanStyle()) {
        originalToTransformed[index.coerceIn(0, source.length)] = output.length
        transformedToOriginal += index.coerceIn(0, source.length)
        output.pushStyle(style)
        output.append(source[index])
        output.pop()
        originalToTransformed[(index + 1).coerceIn(0, source.length)] = output.length
    }

    private fun finishOffsets() {
        var last = 0
        for (index in originalToTransformed.indices) {
            if (originalToTransformed[index] == UNSET_OFFSET) {
                originalToTransformed[index] = last
            } else {
                last = originalToTransformed[index]
            }
        }
        originalToTransformed[source.length] = output.length
    }

    private data class DisplayLineMarker(
        val prefixStart: Int,
        val contentStart: Int,
        val visualPrefix: String,
        val prefixStyle: SpanStyle,
        val contentStyle: SpanStyle
    )

    private companion object {
        const val UNSET_OFFSET = -1
        const val CODE_FENCE = "```"
        const val TODO_UNCHECKED = "\u2610 "
        const val TODO_CHECKED = "\u2611 "
        const val BULLET_PREFIX = "\u2022 "
        const val QUOTE_PREFIX = "│ "
        const val CODE_BLOCK_INDENT = "  "
        const val CODE_BLOCK_PLACEHOLDER = "输入代码"

        val HEADING_STYLE = SpanStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        val ITALIC_STYLE = SpanStyle(
            fontStyle = FontStyle.Italic,
            fontSynthesis = FontSynthesis.Style,
            textGeometricTransform = TextGeometricTransform(skewX = -0.24f)
        )
        val TODO_STYLE = SpanStyle(
            color = AppleNotesStyle.Accent,
            fontWeight = FontWeight.SemiBold
        )
        val LIST_STYLE = SpanStyle(
            color = AppleNotesStyle.Accent,
            fontWeight = FontWeight.Bold
        )
        val QUOTE_BAR_STYLE = SpanStyle(
            color = AppleNotesStyle.Accent,
            fontWeight = FontWeight.Bold
        )
        val QUOTE_TEXT_STYLE = SpanStyle(
            color = AppleNotesStyle.SecondaryText,
            fontStyle = FontStyle.Italic,
            fontSynthesis = FontSynthesis.Style,
            textGeometricTransform = TextGeometricTransform(skewX = -0.16f)
        )
        val INLINE_CODE_STYLE = SpanStyle(
            fontFamily = FontFamily.Monospace,
            background = AppleNotesStyle.SearchSurface
        )
        val CODE_BLOCK_STYLE = SpanStyle(
            fontFamily = FontFamily.Monospace,
            background = AppleNotesStyle.SearchSurface,
            color = AppleNotesStyle.PrimaryText
        )
        val PLACEHOLDER_STYLE = SpanStyle(
            color = AppleNotesStyle.TertiaryText,
            fontStyle = FontStyle.Italic,
            fontSynthesis = FontSynthesis.Style
        )
    }
}

internal fun hasRichTextMarker(text: String): Boolean {
    text.forEach { character ->
        when (character) {
            '*', '`', '#', '>', '-', '[', '~' -> return true
        }
    }
    return false
}
