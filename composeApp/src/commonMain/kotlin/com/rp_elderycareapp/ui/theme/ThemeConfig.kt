package com.rp_elderycareapp.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized theme configuration and constants for the elderly care app
 */
object ThemeConfig {
    // Spacing
    object Spacing {
        val XSmall = 4.dp
        val Small = 8.dp
        val Medium = 12.dp
        val Large = 16.dp
        val XLarge = 24.dp
        val XXLarge = 32.dp
    }

    // Padding values optimized for elderly users (spacious)
    object Padding {
        val Screen = 24.dp        // Main screen padding
        val Card = 16.dp          // Card internal padding
        val Button = 16.dp        // Button internal padding
        val Section = 24.dp       // Section spacing
    }

    // Rounded corners (Apple-aesthetic)
    object Radius {
        val Small = 8.dp
        val Medium = 12.dp
        val Large = 16.dp
        val XLarge = 20.dp
        val Full = 28.dp          // For circular elements
    }

    // Elevation (soft blue-tinted shadows)
    object Elevation {
        val Small = 2.dp
        val Medium = 4.dp
        val Large = 8.dp
        val XLarge = 12.dp
    }

    // Touch target sizes (minimum 56dp for accessibility)
    object TouchTarget {
        val Minimum = 56.dp       // Minimum touch target size
        val Large = 64.dp         // Large buttons
        val Icon = 48.dp          // Icon touch size
    }

    // Card dimensions
    object CardDimensions {
        val DefaultHeight = 140.dp
        val CornerRadius = Radius.XLarge
        val CardPadding = Padding.Card
        val CardElevation = Elevation.Medium
    }

    // Button dimensions
    object ButtonDimensions {
        val Height = 56.dp        // Large touch target
        val CornerRadius = 24.dp  // More rounded corners
        val ButtonPadding = Padding.Button
    }

    // Grid layout
    object Grid {
        val Columns = 2
        val Spacing = 12.dp
        val ItemHeight = 140.dp
    }

    // Profile icon
    object ProfileIcon {
        val Size = 56.dp
        val CircleRadius = 28.dp  // Full circular (Size / 2)
    }

    // Stats card
    object StatsCard {
        val Height = 160.dp
        val CornerRadius = Radius.XLarge
        val Padding = 16.dp
        val IconSize = 32.dp
    }

    // Shadow configuration for blue aesthetic
    object Shadow {
        val Alpha = 0.1f          // 10% opacity for shadows
        val BlueAlpha = 0.08f     // 8% opacity for blue shadows (softer)
    }
}

/**
 * Animated elevation values for interactive elements
 */
object AnimatedElevation {
    val Default = 2.dp
    val Pressed = 1.dp
    val Hovered = 4.dp
}
