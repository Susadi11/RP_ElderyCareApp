package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors

@Composable
expect fun SettingsNotificationIcon()

@Composable
expect fun SettingsVolumeIcon()

@Composable
expect fun SettingsTextIcon()

@Composable
expect fun SettingsDarkModeIcon()

@Composable
expect fun SettingsActivityIcon()

@Composable
expect fun SettingsHeartIcon()

@Composable
expect fun SettingsHealthMetricsIcon()

@Composable
expect fun SettingsHelpIcon()

@Composable
expect fun SettingsShieldIcon()

@Composable
expect fun SettingsInfoIcon()

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var largeTextEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    
    // Ash shade color for all icon backgrounds
    val ashColor = Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        SettingsHeader(onNavigateBack = onNavigateBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // General Settings
            item {
                SettingsSectionTitle("General")
                SettingsCard {
                    Column {
                        SettingsToggleItem(
                            title = "Notifications",
                            subtitle = "Enable push notifications",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            iconColor = ashColor,
                            icon = { SettingsNotificationIcon() }
                        )
                        SettingsDivider()
                        SettingsToggleItem(
                            title = "Sound",
                            subtitle = "Enable sound effects",
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            iconColor = ashColor,
                            icon = { SettingsVolumeIcon() }
                        )
                        SettingsDivider()
                        SettingsToggleItem(
                            title = "Large Text",
                            subtitle = "Easier to read text",
                            checked = largeTextEnabled,
                            onCheckedChange = { largeTextEnabled = it },
                            iconColor = ashColor,
                            icon = { SettingsTextIcon() }
                        )
                        SettingsDivider()
                        SettingsToggleItem(
                            title = "Dark Mode",
                            subtitle = "Reduce eye strain",
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it },
                            iconColor = ashColor,
                            icon = { SettingsDarkModeIcon() }
                        )
                    }
                }
            }

            // Health & Wellness
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionTitle("Health & Wellness")
                SettingsCard {
                    Column {
                        SettingsNavigationItem(
                            title = "Activity Goals",
                            subtitle = "Set daily activity targets",
                            iconColor = ashColor,
                            onClick = { /* Navigate to activity goals */ },
                            icon = { SettingsActivityIcon() }
                        )
                        SettingsDivider()
                        SettingsNavigationItem(
                            title = "Medications",
                            subtitle = "Manage your medications",
                            iconColor = ashColor,
                            onClick = { /* Navigate to medications */ },
                            icon = { SettingsHeartIcon() }
                        )
                        SettingsDivider()
                        SettingsNavigationItem(
                            title = "Health Data",
                            subtitle = "View your health metrics",
                            iconColor = ashColor,
                            onClick = { /* Navigate to health data */ },
                            icon = { SettingsHealthMetricsIcon() }
                        )
                    }
                }
            }

            // Support & About
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionTitle("Support & About")
                SettingsCard {
                    Column {
                        SettingsNavigationItem(
                            title = "Help & Support",
                            subtitle = "Get help with the app",
                            iconColor = ashColor,
                            onClick = { /* Navigate to help */ },
                            icon = { SettingsHelpIcon() }
                        )
                        SettingsDivider()
                        SettingsNavigationItem(
                            title = "Privacy Policy",
                            subtitle = "Review our privacy policy",
                            iconColor = ashColor,
                            onClick = { /* Navigate to privacy */ },
                            icon = { SettingsShieldIcon() }
                        )
                        SettingsDivider()
                        SettingsNavigationItem(
                            title = "About",
                            subtitle = "App version 1.0.0",
                            iconColor = ashColor,
                            onClick = { /* Navigate to about */ },
                            icon = { SettingsInfoIcon() }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.Primary.copy(alpha = 0.1f),
                        AppColors.LightBlue.copy(alpha = 0.05f)
                    )
                )
            )
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColors.Primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.DeepBlue
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF9CA3AF),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        content()
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.DeepBlue
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.DeepBlue
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
        }

        Icon(
            imageVector = Icons.Filled.NavigateNext,
            contentDescription = "Navigate",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    Divider(
        modifier = Modifier.padding(start = 68.dp),
        color = Color(0xFFE5E7EB),
        thickness = 0.5.dp
    )
}
