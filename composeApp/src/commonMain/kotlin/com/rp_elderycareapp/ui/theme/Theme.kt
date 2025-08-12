package com.rp_elderycareapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // Primary colors - using Soft Blue
    primary = AppColors.SoftBlue,
    onPrimary = Color.White,
    primaryContainer = AppColors.WarmBeige,
    onPrimaryContainer = AppColors.PrimaryText,
    
    // Secondary colors - using Soft Green
    secondary = AppColors.SoftGreen,
    onSecondary = Color.White,
    secondaryContainer = AppColors.WarmBeige.copy(alpha = 0.3f),
    onSecondaryContainer = AppColors.PrimaryText,
    
    // Tertiary colors - using Muted Red for warnings
    tertiary = AppColors.MutedRed,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.MutedRed.copy(alpha = 0.1f),
    onTertiaryContainer = AppColors.PrimaryText,
    
    // Background colors
    background = AppColors.Background,
    onBackground = AppColors.PrimaryText,
    
    // Surface colors
    surface = AppColors.Surface,
    onSurface = AppColors.PrimaryText,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.SecondaryText,
    
    // Container colors
    surfaceContainer = AppColors.WarmBeige.copy(alpha = 0.2f),
    surfaceContainerHigh = AppColors.WarmBeige.copy(alpha = 0.4f),
    surfaceContainerHighest = AppColors.WarmBeige.copy(alpha = 0.6f),
    
    // Error colors
    error = AppColors.MutedRed,
    onError = Color.White,
    errorContainer = AppColors.MutedRed.copy(alpha = 0.1f),
    onErrorContainer = AppColors.MutedRed,
    
    // Outline colors
    outline = AppColors.SecondaryText.copy(alpha = 0.5f),
    outlineVariant = AppColors.SecondaryText.copy(alpha = 0.3f)
)

private val DarkColorScheme = darkColorScheme(
    // For now, using the same light scheme
    // Can be customized later for dark mode if needed
    primary = AppColors.SoftBlue,
    onPrimary = Color.White,
    primaryContainer = AppColors.WarmBeige,
    onPrimaryContainer = AppColors.PrimaryText,
    
    secondary = AppColors.SoftGreen,
    onSecondary = Color.White,
    
    tertiary = AppColors.MutedRed,
    onTertiary = Color.White,
    
    background = AppColors.Background,
    onBackground = AppColors.PrimaryText,
    
    surface = AppColors.Surface,
    onSurface = AppColors.PrimaryText,
    
    error = AppColors.MutedRed,
    onError = Color.White
)

@Composable
fun ElderyCareTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
