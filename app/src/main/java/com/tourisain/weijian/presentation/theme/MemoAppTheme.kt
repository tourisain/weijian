package com.tourisain.weijian.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified

private val DarkColorScheme = darkColorScheme(
    primary = Primary500,
    secondary = Secondary500,
    tertiary = Accent500,
    background = Neutral900,
    surface = Neutral800,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Error500,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    secondary = Secondary500,
    tertiary = Accent500,
    background = Neutral50,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Neutral900,
    onSurface = Neutral900,
    error = Error500,
    onError = Color.White
)

val IosLightColorScheme = lightColorScheme(
    primary = IosBlue,
    secondary = IosGreen,
    tertiary = IosPurple,
    background = IosBackground,
    surface = IosCard,
    surfaceVariant = IosCardSecondary,
    onPrimary = IosTextOnPrimary,
    onSecondary = IosTextOnSecondary,
    onTertiary = IosTextOnTertiary,
    onBackground = IosTextPrimary,
    onSurface = IosTextPrimary,
    onSurfaceVariant = IosTextSecondary,
    error = IosRed,
    onError = IosTextOnError
)

val IosDarkColorScheme = darkColorScheme(
    primary = IosBlueDark,
    secondary = IosGreenDark,
    tertiary = IosPurpleDark,
    background = IosBackgroundDark,
    surface = IosCardDark,
    surfaceVariant = IosCardSecondaryDark,
    onPrimary = IosTextOnPrimaryDark,
    onSecondary = IosTextOnSecondaryDark,
    onTertiary = IosTextOnTertiaryDark,
    onBackground = IosTextPrimaryDark,
    onSurface = IosTextPrimaryDark,
    onSurfaceVariant = IosTextSecondaryDark,
    error = IosRedDark,
    onError = IosTextOnErrorDark
)

private val PaperLightColorScheme = lightColorScheme(
    primary = Color(0xFFFFB800),
    secondary = Color(0xFF3A7D44),
    tertiary = Color(0xFF5A6F8F),
    background = Color(0xFFFAF8F2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0EDE5),
    onPrimary = Color(0xFF1D1D1F),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1D1D1F),
    onSurface = Color(0xFF1D1D1F),
    onSurfaceVariant = Color(0xFF6E6E73),
    error = Error500,
    onError = Color.White
)

private val SageLightColorScheme = lightColorScheme(
    primary = Color(0xFF4B8F6A),
    secondary = Color(0xFFFFB800),
    tertiary = Color(0xFF5E7A9A),
    background = Color(0xFFF3F7F1),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7EFE3),
    onPrimary = Color.White,
    onSecondary = Color(0xFF1D1D1F),
    onTertiary = Color.White,
    onBackground = Color(0xFF1D1D1F),
    onSurface = Color(0xFF1D1D1F),
    onSurfaceVariant = Color(0xFF5F6F62),
    error = Error500,
    onError = Color.White
)

private val GraphiteDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFC44D),
    secondary = Color(0xFF64D2A0),
    tertiary = Color(0xFF9BB7FF),
    background = Color(0xFF111113),
    surface = Color(0xFF1F1F22),
    surfaceVariant = Color(0xFF2D2D31),
    onPrimary = Color(0xFF1D1D1F),
    onSecondary = Color(0xFF111113),
    onTertiary = Color(0xFF111113),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFC7C7CC),
    error = Color(0xFFFF5A5F),
    onError = Color.White
)

@Composable
fun MemoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themeMode: String = "classic",
    appFont: String = "system",
    textScale: String = "normal",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == "paper" && !darkTheme -> PaperLightColorScheme
        themeMode == "sage" && !darkTheme -> SageLightColorScheme
        themeMode == "graphite" -> GraphiteDarkColorScheme
        themeMode == "ios" && darkTheme -> IosDarkColorScheme
        themeMode == "ios" -> IosLightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = (if (themeMode == "ios") IosTypography else Typography).withAppearance(appFont, textScale),
        content = content
    )
}

private fun Typography.withAppearance(appFont: String, textScale: String): Typography {
    val family = when (appFont) {
        "serif" -> FontFamily.Serif
        "mono" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
    val scale = when (textScale) {
        "compact" -> 0.92f
        "large" -> 1.12f
        else -> 1f
    }
    return copy(
        displayLarge = displayLarge.applyAppearance(family, scale),
        displayMedium = displayMedium.applyAppearance(family, scale),
        displaySmall = displaySmall.applyAppearance(family, scale),
        headlineLarge = headlineLarge.applyAppearance(family, scale),
        headlineMedium = headlineMedium.applyAppearance(family, scale),
        headlineSmall = headlineSmall.applyAppearance(family, scale),
        titleLarge = titleLarge.applyAppearance(family, scale),
        titleMedium = titleMedium.applyAppearance(family, scale),
        titleSmall = titleSmall.applyAppearance(family, scale),
        bodyLarge = bodyLarge.applyAppearance(family, scale),
        bodyMedium = bodyMedium.applyAppearance(family, scale),
        bodySmall = bodySmall.applyAppearance(family, scale),
        labelLarge = labelLarge.applyAppearance(family, scale),
        labelMedium = labelMedium.applyAppearance(family, scale),
        labelSmall = labelSmall.applyAppearance(family, scale)
    )
}

private fun TextStyle.applyAppearance(fontFamily: FontFamily, scale: Float): TextStyle {
    return copy(
        fontFamily = fontFamily,
        fontSize = fontSize.scaledBy(scale),
        lineHeight = lineHeight.scaledBy(scale)
    )
}

private fun TextUnit.scaledBy(scale: Float): TextUnit {
    return if (isSpecified) this * scale else this
}
