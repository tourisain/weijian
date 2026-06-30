package com.tourisain.weijian.presentation.note.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppleNotesStyle {
    var Background by mutableStateOf(Color(0xFFF7F5EF))
        private set
    var Surface by mutableStateOf(Color(0xFFFFFFFF))
        private set
    var SearchSurface by mutableStateOf(Color(0xFFEDEAE3))
        private set
    var Separator by mutableStateOf(Color(0x1F000000))
        private set
    var PrimaryText by mutableStateOf(Color(0xFF1D1D1F))
        private set
    var SecondaryText by mutableStateOf(Color(0xFF6E6E73))
        private set
    var TertiaryText by mutableStateOf(Color(0xFF9A9A9F))
        private set
    var Accent by mutableStateOf(Color(0xFFFFB800))
        private set
    var AccentSoft by mutableStateOf(Color(0xFFFFF4C2))
        private set
    var Destructive by mutableStateOf(Color(0xFFE5484D))
        private set

    const val GroupRadiusDp = 12
    const val SearchRadiusDp = 10
    const val SettingsRowVerticalPaddingDp = 10
    const val NoteRowVerticalPaddingDp = 10
    const val ListIconSizeDp = 30
    const val ListIconGlyphSizeDp = 17
    const val DialogMaxHeightDp = 320

    val GroupShape = RoundedCornerShape(GroupRadiusDp.dp)
    val SearchShape = RoundedCornerShape(SearchRadiusDp.dp)
    val ButtonShape = RoundedCornerShape(16.dp)

    fun applyAppearance(
        darkTheme: Boolean,
        style: String,
        dynamicBackground: Color? = null,
        dynamicSurface: Color? = null,
        dynamicSearchSurface: Color? = null,
        dynamicAccent: Color? = null
    ) {
        val apple = style == "ios"
        val paper = style == "paper"
        val sage = style == "sage"
        val graphite = style == "graphite"
        Background = dynamicBackground ?: when {
            graphite -> Color(0xFF111113)
            darkTheme -> Color(0xFF1C1C1E)
            apple -> Color(0xFFF2F2F7)
            paper -> Color(0xFFFAF8F2)
            sage -> Color(0xFFF3F7F1)
            else -> Color(0xFFF7F5EF)
        }
        Surface = dynamicSurface ?: when {
            graphite -> Color(0xFF1F1F22)
            darkTheme -> Color(0xFF2C2C2E)
            else -> Color.White
        }
        SearchSurface = dynamicSearchSurface ?: when {
            graphite -> Color(0xFF2D2D31)
            darkTheme -> Color(0xFF3A3A3C)
            apple -> Color(0xFFE5E5EA)
            paper -> Color(0xFFF0EDE5)
            sage -> Color(0xFFE7EFE3)
            else -> Color(0xFFEDEAE3)
        }
        Separator = if (darkTheme || graphite) Color(0x33FFFFFF) else Color(0x1F000000)
        PrimaryText = if (darkTheme || graphite) Color.White else Color(0xFF1D1D1F)
        SecondaryText = when {
            darkTheme || graphite -> Color(0xFFC7C7CC)
            sage -> Color(0xFF5F6F62)
            else -> Color(0xFF6E6E73)
        }
        TertiaryText = if (darkTheme || graphite) Color(0xFF8E8E93) else Color(0xFF9A9A9F)
        Accent = dynamicAccent ?: when {
            sage -> Color(0xFF4B8F6A)
            graphite -> Color(0xFFFFC44D)
            else -> Color(0xFFFFB800)
        }
        AccentSoft = dynamicAccent?.copy(alpha = if (darkTheme) 0.24f else 0.16f)
            ?: when {
                graphite -> Color(0xFF3A3116)
                darkTheme -> Color(0xFF4A3A10)
                sage -> Color(0xFFDDEBDD)
                else -> Color(0xFFFFF4C2)
            }
        Destructive = if (darkTheme || graphite) Color(0xFFFF5A5F) else Color(0xFFE5484D)
    }
}
