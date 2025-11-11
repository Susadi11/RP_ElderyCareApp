package com.rp_elderycareapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.ui.theme.ThemeConfig

@Composable
actual fun StatsGridContent(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ThemeConfig.Padding.Screen)
    ) {
        // First row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = ThemeConfig.Grid.Spacing),
            horizontalArrangement = Arrangement.spacedBy(ThemeConfig.Grid.Spacing)
        ) {
            StatsCard(
                icon = { StatIcon(Icons.Default.Favorite, Color(0xFFEF4444)) },
                statNumber = "85%",
                label = "Health Score",
                trendValue = "+3%",
                trendIsPositive = true,
                modifier = Modifier.weight(1f)
            )

            StatsCard(
                icon = { StatIcon(Icons.Default.Star, Color(0xFF10B981)) },
                statNumber = "28",
                label = "Activities Done",
                trendValue = "+4",
                trendIsPositive = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ThemeConfig.Grid.Spacing)
        ) {
            StatsCard(
                icon = { StatIcon(Icons.Default.PlayArrow, Color(0xFF8B5CF6)) },
                statNumber = "12",
                label = "Games Played",
                trendValue = "+2",
                trendIsPositive = true,
                modifier = Modifier.weight(1f)
            )

            StatsCard(
                icon = { StatIcon(Icons.Default.DateRange, Color(0xFFF59E0B)) },
                statNumber = "3",
                label = "Upcoming Tasks",
                trendValue = "Today",
                trendIsPositive = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
