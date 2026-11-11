package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// TradeSource B2B Wholesale Theme Colors
val TradeNavyDark = Color(0xFF0A0F1D)
val TradeSurfaceDark = Color(0xFF131B2E)
val TradeCardDark = Color(0xFF1B253D)
val TradeOutlineDark = Color(0xFF2C3B5E)

val TradeBgLight = Color(0xFFF6F8FC)
val TradeSurfaceLight = Color(0xFFFFFFFF)
val TradeCardLight = Color(0xFFF1F4FA)
val TradeOutlineLight = Color(0xFFD2DCEE)

// Brand Accents
val TradePrimary = Color(0xFF0284C7) // Sky / Cyan Blue
val TradePrimaryLight = Color(0xFF38BDF8)
val TradePrimaryDark = Color(0xFF0369A1)

val TradeGold = Color(0xFFF59E0B) // Gold Supplier
val TradeGoldLight = Color(0xFFFDE68A)
val TradeGoldDark = Color(0xFFB45309)

val TradeEmerald = Color(0xFF10B981) // Verified Factory
val TradeEmeraldLight = Color(0xFFA7F3D0)
val TradeEmeraldDark = Color(0xFF047857)

val TradeBlue = Color(0xFF2563EB) // Trade Assurance
val TradeCoral = Color(0xFFF43F5E) // Hot deals / Low MOQ
val TradePurple = Color(0xFF8B5CF6) // OEM/ODM custom mold

/**
 * Extended semantic custom colors for B2B Wholesale domain
 */
@Immutable
data class TradeCustomColors(
    val goldBadge: Color = TradeGold,
    val goldBadgeContainer: Color = TradeGoldLight,
    val verifiedBadge: Color = TradeEmerald,
    val verifiedBadgeContainer: Color = TradeEmeraldLight,
    val tradeAssurance: Color = TradeBlue,
    val moqAccent: Color = TradeCoral,
    val oemAccent: Color = TradePurple,
    val cardSurface: Color = TradeCardDark
)

val LocalTradeCustomColors = staticCompositionLocalOf { TradeCustomColors() }
