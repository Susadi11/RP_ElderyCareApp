package com.rp_elderycareapp.screens

import androidx.compose.foundation.BorderStroke
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
import kotlinx.coroutines.launch
import com.rp_elderycareapp.viewmodel.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.PreferencesManager

@Composable
expect fun ProfileUserIcon()

@Composable
expect fun ProfileEditIcon()

@Composable
expect fun ProfileLockIcon()

@Composable
expect fun ProfileDeleteIcon()

@Composable
expect fun ProfileLogoutIcon()

@Composable
expect fun ProfileSettingsIcon()

@Composable
expect fun getPreferencesManager(): PreferencesManager

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val preferencesManager = getPreferencesManager()
    var showBaseUrlDialog by remember { mutableStateOf(false) }
    var baseUrlInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Observe user data
    val currentUser = authViewModel.currentUser.value
    val isLoading = authViewModel.isLoading.value
    
    // Load profile on first launch
    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        ProfileHeader(onNavigateBack = onNavigateBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Avatar and Name
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        AppColors.Primary,
                                        AppColors.LightBlue
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileUserIcon()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentUser?.full_name ?: "Loading...",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DeepBlue
                    )

                    Text(
                        text = currentUser?.email ?: "",
                        fontSize = 16.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit Profile Button
                    OutlinedButton(
                        onClick = { /* Edit profile */ },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Primary
                        )
                    ) {
                        ProfileEditIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }

            // User Details Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Personal Information",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column {
                        ProfileDetailItem(
                            label = "Full Name",
                            value = currentUser?.full_name ?: "Not set"
                        )
                        ProfileDivider()
                        ProfileDetailItem(
                            label = "Email",
                            value = currentUser?.email ?: "Not set"
                        )
                        ProfileDivider()
                        ProfileDetailItem(
                            label = "Phone",
                            value = currentUser?.phone_number?.takeIf { it.isNotEmpty() } ?: "Not set"
                        )
                        if (currentUser?.age != null) {
                            ProfileDivider()
                            ProfileDetailItem(
                                label = "Age",
                                value = currentUser.age.toString()
                            )
                        }
                        if (currentUser?.emergency_contact_number?.isNotEmpty() == true) {
                            ProfileDivider()
                            ProfileDetailItem(
                                label = "Emergency Contact",
                                value = currentUser.emergency_contact_number
                            )
                        }
                    }
                }
            }

            // Account Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column {
                        ProfileActionItem(
                            title = "Change Password",
                            iconColor = AppColors.Primary,
                            onClick = { /* Change password */ },
                            icon = { ProfileLockIcon() }
                        )
                        ProfileDivider()
                        ProfileActionItem(
                            title = "Change Base URL",
                            iconColor = AppColors.Primary,
                            onClick = {
                                baseUrlInput = preferencesManager.getBaseUrl() ?: ""
                                showBaseUrlDialog = true
                            },
                            icon = { ProfileSettingsIcon() }
                        )
                        ProfileDivider()
                        ProfileActionItem(
                            title = "Delete Account",
                            iconColor = Color(0xFFEF4444),
                            onClick = { /* Delete account */ },
                            icon = { ProfileDeleteIcon() }
                        )
                    }
                }
            }

            // Logout Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFB91C1C),
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFFB91C1C)
                    )
                ) {
                    ProfileLogoutIcon()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Log Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Base URL Dialog
    if (showBaseUrlDialog) {
        AlertDialog(
            onDismissRequest = { showBaseUrlDialog = false },
            title = {
                Text(
                    text = "Change Base URL",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the base URL (including port number):",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = baseUrlInput,
                        onValueChange = { baseUrlInput = it },
                        placeholder = { Text("http://localhost:8000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Leave empty to use default URL",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (baseUrlInput.isNotBlank()) {
                            preferencesManager.saveBaseUrl(baseUrlInput.trim())
                        } else {
                            preferencesManager.clearBaseUrl()
                        }
                        showBaseUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBaseUrlDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ProfileHeader(
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
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.DeepBlue
            )
        }
    }
}

@Composable
private fun ProfileDetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = AppColors.DeepBlue,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileActionItem(
    title: String,
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.DeepBlue,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Filled.NavigateNext,
            contentDescription = "Navigate",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = Color(0xFFE5E7EB),
        thickness = 0.5.dp
    )
}
