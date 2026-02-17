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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.PreferencesManager
import com.rp_elderycareapp.components.ImagePickerLauncher
import com.rp_elderycareapp.components.rememberImagePickerLauncher
import coil3.compose.AsyncImage

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
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Observe user data
    val currentUser = authViewModel.currentUser.value
    val isLoading = authViewModel.isLoading.value
    val successMessage = authViewModel.successMessage.value
    val errorMessage = authViewModel.errorMessage.value

    // Profile photo state - use a key to force reload after upload
    var photoRefreshKey by remember { mutableStateOf(0) }

    // Image picker for profile photo
    val imagePickerLauncher = rememberImagePickerLauncher { base64String ->
        if (base64String != null) {
            scope.launch {
                authViewModel.uploadProfilePhoto(base64String)
                photoRefreshKey++ // Force reload of the photo
            }
        }
    }

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
                    // Profile Photo with upload capability
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.has_profile_photo == true) {
                            // Load profile photo from server
                            val photoUrl = remember(currentUser.user_id, photoRefreshKey) {
                                authViewModel.getProfilePhotoUrl(currentUser.user_id) + "?t=$photoRefreshKey"
                            }
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch() },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Default avatar with gradient
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
                                    )
                                    .clickable { imagePickerLauncher.launch() },
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileUserIcon()
                            }
                        }

                        // Camera icon overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppColors.Primary)
                                .clickable { imagePickerLauncher.launch() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
                        onClick = { showEditProfileDialog = true },
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

            // Status messages
            item {
                successMessage?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = Color(0xFF10B981), fontSize = 14.sp)
                        }
                    }
                }

                errorMessage?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = Color(0xFFEF4444), fontSize = 14.sp)
                        }
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
                        ProfileDivider()
                        ProfileDetailItem(
                            label = "Age",
                            value = currentUser?.age?.toString() ?: "Not set"
                        )
                        ProfileDivider()
                        ProfileDetailItem(
                            label = "Gender",
                            value = currentUser?.gender?.takeIf { it.isNotEmpty() } ?: "Not set"
                        )
                        ProfileDivider()
                        ProfileDetailItem(
                            label = "Address",
                            value = currentUser?.address?.takeIf { it.isNotEmpty() } ?: "Not set"
                        )
                        ProfileDivider()
                        ProfileDetailItem(
                            label = "Emergency Contact",
                            value = if (!currentUser?.emergency_contact_name.isNullOrEmpty()) {
                                "${currentUser?.emergency_contact_name} (${currentUser?.emergency_contact_number ?: ""})"
                            } else {
                                "Not set"
                            }
                        )
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

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showEditProfileDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            EditProfileDialog(
                currentUser = currentUser,
                isLoading = isLoading,
                onDismiss = { showEditProfileDialog = false },
                onSave = { fullName, phone, age, gender, address, emergencyName, emergencyNumber ->
                    scope.launch {
                        authViewModel.updateProfile(
                            fullName = fullName,
                            phoneNumber = phone,
                            age = age,
                            gender = gender,
                            address = address,
                            emergencyContactName = emergencyName,
                            emergencyContactNumber = emergencyNumber
                        )
                        showEditProfileDialog = false
                    }
                }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    currentUser: com.rp_elderycareapp.api.UserProfile?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        fullName: String?,
        phone: String?,
        age: Int?,
        gender: String?,
        address: String?,
        emergencyName: String?,
        emergencyNumber: String?
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser?.full_name ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone_number ?: "") }
    var ageText by remember { mutableStateOf(currentUser?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(currentUser?.gender ?: "") }
    var address by remember { mutableStateOf(currentUser?.address ?: "") }
    var emergencyName by remember { mutableStateOf(currentUser?.emergency_contact_name ?: "") }
    var emergencyNumber by remember { mutableStateOf(currentUser?.emergency_contact_number ?: "") }
    var genderExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.Primary,
        unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
        focusedLabelColor = AppColors.Primary,
        cursorColor = AppColors.Primary
    )

    // Full-screen overlay dialog
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
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
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DeepBlue,
                        fontSize = 22.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9CA3AF).copy(alpha = 0.1f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Scrollable form fields
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = AppColors.Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = AppColors.Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                    ageText = newValue
                                }
                            },
                            label = { Text("Age") },
                            leadingIcon = { Icon(Icons.Default.Cake, null, tint = AppColors.Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                leadingIcon = { Icon(Icons.Default.Face, null, tint = AppColors.Primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                listOf("Male", "Female", "Other").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            gender = option
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            leadingIcon = { Icon(Icons.Default.Home, null, tint = AppColors.Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = emergencyName,
                            onValueChange = { emergencyName = it },
                            label = { Text("Emergency Contact Name") },
                            leadingIcon = { Icon(Icons.Default.ContactPhone, null, tint = AppColors.Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = emergencyNumber,
                            onValueChange = { emergencyNumber = it },
                            label = { Text("Emergency Contact Number") },
                            leadingIcon = { Icon(Icons.Default.Call, null, tint = AppColors.Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                }

                // Bottom buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            onSave(
                                fullName.takeIf { it.isNotBlank() },
                                phone.takeIf { it.isNotBlank() },
                                ageText.toIntOrNull(),
                                gender.takeIf { it.isNotBlank() },
                                address.takeIf { it.isNotBlank() },
                                emergencyName.takeIf { it.isNotBlank() },
                                emergencyNumber.takeIf { it.isNotBlank() }
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Changes", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
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
