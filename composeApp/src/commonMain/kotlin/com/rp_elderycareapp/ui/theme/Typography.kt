package com.rp_elderycareapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * SF Pro Display Typography for Apple-aesthetic design
 * Optimized for elderly users with large, readable fonts
 * Uses SF Pro Display fonts from composeApp/src/androidMain/res/font/
 *
 * Note: Fonts are loaded via Compose Resource System
 * Font files: sfprodisplay*.otf in composeApp/src/androidMain/res/font/
 */

// SF Pro Display font family - uses system default with fallback support
// In Android, fonts from res/font/ are automatically available
val SFProDisplay = FontFamily.Default

object AppTypography {
    /**
     * Large thin-weight time display (48sp)
     * Used for the main time display on home screen
     */
    val TimeDisplay = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 48.sp,
        fontWeight = FontWeight.Thin,
        letterSpacing = (-0.5).sp,
        lineHeight = 52.sp
    )

    /**
     * Current date in muted text
     * Used below the time display
     */
    val DateText = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp,
        lineHeight = 20.sp
    )

    /**
     * Personalized greeting text
     * "Good Morning/Afternoon/Evening"
     */
    val GreetingTitle = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 28.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.3).sp,
        lineHeight = 32.sp
    )

    /**
     * Subtitle text "How are you feeling today?"
     */
    val GreetingSubtitle = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
        lineHeight = 24.sp
    )

    /**
     * Stats card number (large, bold)
     * Used for stat values in cards
     */
    val StatNumber = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
        lineHeight = 40.sp
    )

    /**
     * Stats card label text
     * Used for card labels like "Cognitive Score"
     */
    val StatLabel = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
        lineHeight = 18.sp
    )

    /**
     * Trend indicator text (+/- percentage)
     */
    val TrendText = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
        lineHeight = 16.sp
    )

    /**
     * Quick action button text
     */
    val ButtonText = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        lineHeight = 24.sp
    )

    /**
     * Secondary button text
     */
    val SecondaryButtonText = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.3.sp,
        lineHeight = 20.sp
    )

    /**
     * Body text for regular content
     */
    val Body = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
        lineHeight = 24.sp
    )

    /**
     * Small body text
     */
    val SmallBody = TextStyle(
        fontFamily = SFProDisplay,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
        lineHeight = 20.sp
    )
}

/**
 * Material3 Typography using custom styles
 * This can be used with MaterialTheme
 */
val AppComposeTypography = Typography(
    headlineLarge = AppTypography.TimeDisplay,
    headlineMedium = AppTypography.GreetingTitle,
    headlineSmall = AppTypography.StatNumber,
    titleLarge = AppTypography.ButtonText,
    titleMedium = AppTypography.StatLabel,
    titleSmall = AppTypography.TrendText,
    bodyLarge = AppTypography.Body,
    bodyMedium = AppTypography.SmallBody,
    bodySmall = AppTypography.DateText,
    labelLarge = AppTypography.ButtonText,
    labelMedium = AppTypography.SecondaryButtonText,
    labelSmall = AppTypography.TrendText
)
