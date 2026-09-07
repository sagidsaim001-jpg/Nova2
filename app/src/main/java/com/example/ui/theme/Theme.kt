package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.repository.AccentColor
import com.example.data.repository.ThemeMode

val LocalNovaTokens = staticCompositionLocalOf<ThemeColorTokens> {
    error("No NovaColorTokens provided")
}

@Composable
fun NovaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.PURPLE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val tokens = getThemeTokens(accentColor, isDark)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = tokens.primary,
            secondary = tokens.secondary,
            tertiary = tokens.tertiary,
            background = tokens.background,
            surface = tokens.surface,
            surfaceVariant = tokens.surfaceElevated,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.border
        )
    } else {
        lightColorScheme(
            primary = tokens.primary,
            secondary = tokens.secondary,
            tertiary = tokens.tertiary,
            background = tokens.background,
            surface = tokens.surface,
            surfaceVariant = tokens.surfaceElevated,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.border
        )
    }

    CompositionLocalProvider(LocalNovaTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
