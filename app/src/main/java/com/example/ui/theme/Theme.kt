package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TradePrimaryLight,
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D68),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = TradeGold,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF3B2700),
    onSecondaryContainer = TradeGoldLight,
    tertiary = TradeEmerald,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF005234),
    onTertiaryContainer = TradeEmeraldLight,
    background = TradeNavyDark,
    onBackground = Color(0xFFE2E8F0),
    surface = TradeSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = TradeCardDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = TradeOutlineDark,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = TradePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = TradeGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = TradeEmeraldDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA7F3D0),
    onTertiaryContainer = Color(0xFF003822),
    background = TradeBgLight,
    onBackground = Color(0xFF0F172A),
    surface = TradeSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = TradeCardLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = TradeOutlineLight,
    outlineVariant = Color(0xFFE2E8F0)
)

/**
 * Accessor for domain-specific trade colors:
 * Example: MaterialTheme.tradeColors.goldBadge
 */
val MaterialTheme.tradeColors: TradeCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalTradeCustomColors.current

/**
 * Centralized Material 3 Theme Wrapper for the TradeSource application.
 * Ensures consistent typography, semantic color schemes, and rounded shapes across all components.
 */
@Composable
fun TradeSourceTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        TradeCustomColors(
            goldBadge = TradeGold,
            goldBadgeContainer = TradeGoldLight,
            verifiedBadge = TradeEmerald,
            verifiedBadgeContainer = TradeEmeraldLight,
            tradeAssurance = TradeBlue,
            moqAccent = TradeCoral,
            oemAccent = TradePurple,
            cardSurface = TradeCardDark
        )
    } else {
        TradeCustomColors(
            goldBadge = TradeGoldDark,
            goldBadgeContainer = Color(0xFFFEF3C7),
            verifiedBadge = TradeEmeraldDark,
            verifiedBadgeContainer = Color(0xFFA7F3D0),
            tradeAssurance = Color(0xFF1D4ED8),
            moqAccent = Color(0xFFE11D48),
            oemAccent = Color(0xFF7C3AED),
            cardSurface = TradeCardLight
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalTradeCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = TradeShapes,
            content = content
        )
    }
}
