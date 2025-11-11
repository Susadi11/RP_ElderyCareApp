package com.rp_elderycareapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.ui.theme.AppTypography
import com.rp_elderycareapp.ui.theme.ThemeConfig
import com.rp_elderycareapp.utils.TimeUtils
import kotlinx.coroutines.delay

/**
 * Home screen header with time display and profile icon
 * Features:
 * - Large thin-weight time display (48sp)
 * - Current date in muted blue-gray
 * - Circular blue gradient profile icon (top right)
 * - Real-time time updates
 */
@Composable
fun HomeHeader(
    userName: String = "User",
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(TimeUtils.getCurrentTimeFormatted()) }
    var currentDate by remember { mutableStateOf(TimeUtils.getCurrentDateFormatted()) }

    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000) // Update every 60 seconds
            currentTime = TimeUtils.getCurrentTimeFormatted()
            currentDate = TimeUtils.getCurrentDateFormatted()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ThemeConfig.Padding.Screen)
            .padding(top = ThemeConfig.Padding.Screen)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Time Display - Large, bold weight
            Text(
                text = currentTime,
                style = AppTypography.TimeDisplay.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryText
                ),
                textAlign = TextAlign.Start
            )

            // Date Display - Semi-bold
            Text(
                text = currentDate,
                style = AppTypography.DateText.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.SecondaryText
                ),
                textAlign = TextAlign.Start
            )
        }

        // Profile Icon - Circular blue gradient (top right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(ThemeConfig.ProfileIcon.Size)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.LightBlue,
                            AppColors.DeepBlue
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = AppTypography.TimeDisplay.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.CardBackground
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Greeting section below header
 * Shows personalized greeting and subtitle
 */
@Composable
fun GreetingSection(
    userName: String = "User",
    modifier: Modifier = Modifier
) {
    val greeting = remember { TimeUtils.getGreetingMessage() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ThemeConfig.Padding.Screen)
            .padding(vertical = ThemeConfig.Padding.Section),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Personalized greeting
        Text(
            text = "$greeting, $userName",
            style = AppTypography.GreetingTitle.copy(
                color = AppColors.PrimaryText,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Subtitle
        Text(
            text = "How are you feeling today?",
            style = AppTypography.GreetingSubtitle.copy(
                color = AppColors.SecondaryText,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
