package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.repository.AccentColor

// Base Dark Backgrounds (Deep Charcoal / Obsidian)
val DarkBackground = Color(0xFF0D0E15)
val DarkSurface = Color(0xFF151722)
val DarkSurfaceElevated = Color(0xFF1E202E)
val DarkSurfaceCard = Color(0xFF26293A)
val DarkBorder = Color(0xFF32364D)
val DarkTextPrimary = Color(0xFFF3F4F8)
val DarkTextSecondary = Color(0xFFA0A5BD)
val DarkTextMuted = Color(0xFF6B728E)

// Base Light Backgrounds (Soft Off-White / Pearl)
val LightBackground = Color(0xFFF8F9FD)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF0F3F9)
val LightSurfaceCard = Color(0xFFE8EDF5)
val LightBorder = Color(0xFFD6DEEB)
val LightTextPrimary = Color(0xFF131521)
val LightTextSecondary = Color(0xFF5A607A)
val LightTextMuted = Color(0xFF8B92AB)

// Accent Palettes
object NovaAccents {
    // Purple (Default) - Cosmic Violet & Radiant Lilac
    val PurplePrimary = Color(0xFF8B5CF6)
    val PurpleSecondary = Color(0xFFA855F7)
    val PurpleTertiary = Color(0xFFEC4899)
    val PurpleContainer = Color(0xFF2E1A47)
    val PurpleLightContainer = Color(0xFFF3E8FF)

    // Blue - Electric Azure & Cyan
    val BluePrimary = Color(0xFF3B82F6)
    val BlueSecondary = Color(0xFF06B6D4)
    val BlueTertiary = Color(0xFF6366F1)
    val BlueContainer = Color(0xFF172554)
    val BlueLightContainer = Color(0xFFE0F2FE)

    // Pink - Radiant Rose & Fuchsia
    val PinkPrimary = Color(0xFFEC4899)
    val PinkSecondary = Color(0xFFF43F5E)
    val PinkTertiary = Color(0xFFD946EF)
    val PinkContainer = Color(0xFF4C0519)
    val PinkLightContainer = Color(0xFFFFE4E6)

    // Green - Emerald Jade & Mint
    val GreenPrimary = Color(0xFF10B981)
    val GreenSecondary = Color(0xFF14B8A6)
    val GreenTertiary = Color(0xFF84CC16)
    val GreenContainer = Color(0xFF022C22)
    val GreenLightContainer = Color(0xFFD1FAE5)

    // Orange - Radiant Amber & Sunset Coral
    val OrangePrimary = Color(0xFFF97316)
    val OrangeSecondary = Color(0xFFFB923C)
    val OrangeTertiary = Color(0xFFE11D48)
    val OrangeContainer = Color(0xFF431407)
    val OrangeLightContainer = Color(0xFFFFEDD5)

    // Cyan - Cyber Neon & Glacier Teal
    val CyanPrimary = Color(0xFF06B6D4)
    val CyanSecondary = Color(0xFF0EA5E9)
    val CyanTertiary = Color(0xFF2DD4BF)
    val CyanContainer = Color(0xFF083344)
    val CyanLightContainer = Color(0xFFCFFAFE)
}

// Brand Gradient
val NovaStarGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFFF59E0B)  // Warm Gold
    )
)

val NovaCardGlowGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x258B5CF6),
        Color(0x05EC4899),
        Color(0x00000000)
    )
)

data class ThemeColorTokens(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val container: Color,
    val lightContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceCard: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean,
    val gradientBrush: Brush
)

fun getThemeTokens(accent: AccentColor, isDark: Boolean): ThemeColorTokens {
    val (prim, sec, tert, cont, lightCont) = when (accent) {
        AccentColor.PURPLE -> Tuple5(
            NovaAccents.PurplePrimary, NovaAccents.PurpleSecondary, NovaAccents.PurpleTertiary,
            NovaAccents.PurpleContainer, NovaAccents.PurpleLightContainer
        )
        AccentColor.BLUE -> Tuple5(
            NovaAccents.BluePrimary, NovaAccents.BlueSecondary, NovaAccents.BlueTertiary,
            NovaAccents.BlueContainer, NovaAccents.BlueLightContainer
        )
        AccentColor.PINK -> Tuple5(
            NovaAccents.PinkPrimary, NovaAccents.PinkSecondary, NovaAccents.PinkTertiary,
            NovaAccents.PinkContainer, NovaAccents.PinkLightContainer
        )
        AccentColor.GREEN -> Tuple5(
            NovaAccents.GreenPrimary, NovaAccents.GreenSecondary, NovaAccents.GreenTertiary,
            NovaAccents.GreenContainer, NovaAccents.GreenLightContainer
        )
        AccentColor.ORANGE -> Tuple5(
            NovaAccents.OrangePrimary, NovaAccents.OrangeSecondary, NovaAccents.OrangeTertiary,
            NovaAccents.OrangeContainer, NovaAccents.OrangeLightContainer
        )
        AccentColor.CYAN -> Tuple5(
            NovaAccents.CyanPrimary, NovaAccents.CyanSecondary, NovaAccents.CyanTertiary,
            NovaAccents.CyanContainer, NovaAccents.CyanLightContainer
        )
    }

    val brush = Brush.linearGradient(
        colors = listOf(prim, sec, tert)
    )

    return if (isDark) {
        ThemeColorTokens(
            primary = prim,
            secondary = sec,
            tertiary = tert,
            container = cont,
            lightContainer = lightCont,
            background = DarkBackground,
            surface = DarkSurface,
            surfaceElevated = DarkSurfaceElevated,
            surfaceCard = DarkSurfaceCard,
            border = DarkBorder,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            isDark = true,
            gradientBrush = brush
        )
    } else {
        ThemeColorTokens(
            primary = prim,
            secondary = sec,
            tertiary = tert,
            container = lightCont,
            lightContainer = lightCont,
            background = LightBackground,
            surface = LightSurface,
            surfaceElevated = LightSurfaceElevated,
            surfaceCard = LightSurfaceCard,
            border = LightBorder,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            isDark = false,
            gradientBrush = brush
        )
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
