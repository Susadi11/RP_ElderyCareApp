package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.components.*
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.ui.theme.ThemeConfig

/**
 * Modern, minimalistic Apple-aesthetic home screen for elderly care app
 * Features:
 * - Clean light blue-tinted background
 * - Time display with date
 * - Personalized greeting
 * - 4 stats cards in grid layout
 * - Quick action buttons with blue gradients
 */
@Composable
fun HomeScreen(
    userName: String = "User",
    authViewModel: com.rp_elderycareapp.viewmodel.AuthViewModel? = null,
    onStartChat: () -> Unit = {},
    onPlayGames: () -> Unit = {},
    onTakeMmseTest: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPatientDetails: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with time and profile icon
            HomeHeader(
                userName = userName,
                onProfileClick = onNavigateToProfile
            )

            // Greeting section
            GreetingSection(userName = userName)

            // Stats cards grid
            StatsGrid()

            Spacer(modifier = Modifier.height(ThemeConfig.Padding.Section))

            // Quick action buttons
            QuickActionsSection(
                onStartChat = onStartChat,
                onPlayGames = onPlayGames,
                onViewProgress = onTakeMmseTest,
                onNavigateToSettings = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(12.dp))

            // My Health Profile Button - matching QuickActionButton style
            QuickActionButton(
                text = "My Health Profile",
                icon = Icons.Default.Favorite,
                backgroundColor = AppColors.Primary,
                onClick = onNavigateToPatientDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeConfig.Padding.Screen)
            )

            // Bottom spacing for safe area
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
