package com.rp_elderycareapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.R
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
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFEF4444), // Red
                        modifier = Modifier.size(20.dp)
                    )
                },
                statNumber = "85%",
                label = "Health Score",
                trendValue = "+3%",
                trendIsPositive = true,
                modifier = Modifier.weight(1f)
            )

            StatsCard(
                icon = { 
                    Icon(
                        painter = painterResource(R.drawable.outline_health_metrics_24),
                        contentDescription = null,
                        tint = Color(0xFF10B981), // Green
                        modifier = Modifier.size(20.dp)
                    )
                },
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
                icon = { 
                    Icon(
                        painter = painterResource(R.drawable.outline_gamepad_circle_down_24),
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6), // Purple
                        modifier = Modifier.size(20.dp)
                    )
                },
                statNumber = "12",
                label = "Games Played",
                trendValue = "+2",
                trendIsPositive = true,
                modifier = Modifier.weight(1f)
            )

            StatsCard(
                icon = { 
                    Icon(
                        painter = painterResource(R.drawable.outline_calendar_month_24),
                        contentDescription = null,
                        tint = Color(0xFFF59E0B), // Yellow/Orange
                        modifier = Modifier.size(20.dp)
                    )
                },
                statNumber = "3",
                label = "Upcoming Tasks",
                trendValue = "Today",
                trendIsPositive = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
