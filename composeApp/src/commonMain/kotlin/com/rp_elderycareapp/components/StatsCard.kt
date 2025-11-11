package com.rp_elderycareapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.ui.theme.AppTypography
import com.rp_elderycareapp.ui.theme.ThemeConfig

/**
 * Stat card component for displaying metrics with glassmorphism design
 * Shows: icon, stat number, label, and trend indicator
 */
@Composable
fun StatsCard(
    icon: @Composable () -> Unit,
    statNumber: String,
    label: String,
    trendValue: String = "+5%",
    trendIsPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(ThemeConfig.StatsCard.Height)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.75f)
                    )
                ),
                shape = RoundedCornerShape(ThemeConfig.StatsCard.CornerRadius)
            )
            .border(
                width = 1.5.dp,
                color = AppColors.LightBlue.copy(alpha = 0.3f),
                shape = RoundedCornerShape(ThemeConfig.StatsCard.CornerRadius)
            )
            .padding(ThemeConfig.StatsCard.Padding)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon at top
            Box(
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stat number - Large, bold, dark blue
            Text(
                text = statNumber,
                style = AppTypography.StatNumber.copy(
                    color = AppColors.DeepBlue,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            // Label - Black-ash color
            Text(
                text = label,
                style = AppTypography.StatLabel.copy(
                    color = AppColors.BlackAsh,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Trend indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trendValue,
                    style = AppTypography.TrendText.copy(
                        color = if (trendIsPositive) AppColors.PositiveTrend else AppColors.NegativeTrend,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Start
                )

                // Small indicator dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (trendIsPositive) AppColors.PositiveTrend else AppColors.NegativeTrend,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

/**
 * Simple icon component for stats cards
 */
@Composable
fun StatIcon(
    icon: ImageVector,
    iconColor: Color = AppColors.LightBlue,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = modifier.size(20.dp)
    )
}

/**
 * Stats grid layout component
 * Displays 4 stats in a 2x2 grid
 */
@Composable
fun StatsGrid(
    modifier: Modifier = Modifier
) {
    StatsGridContent(modifier)
}

@Composable
expect fun StatsGridContent(modifier: Modifier = Modifier)
