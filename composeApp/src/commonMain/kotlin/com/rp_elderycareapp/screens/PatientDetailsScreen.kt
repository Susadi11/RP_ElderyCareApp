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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.ui.theme.ThemeConfig
import com.rp_elderycareapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun PatientDetailsScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val successMessage by viewModel.successMessage
    val profileCompletion by viewModel.profileCompletion
    val medicalRecords by viewModel.medicalRecordsState
    val caregiverLookup by viewModel.caregiverLookupResult
    val currentUser by viewModel.currentUser

    // Form states
    var allergiesText by remember { mutableStateOf("") }
    var treatmentsText by remember { mutableStateOf("") }
    var medicinesText by remember { mutableStateOf("") }
    var medicalHistoryText by remember { mutableStateOf("") }
    var caregiverIdInput by remember { mutableStateOf("") }
    var showCaregiverDialog by remember { mutableStateOf(false) }
    var recordsLoaded by remember { mutableStateOf(false) }

    // Load data on screen entry
    LaunchedEffect(Unit) {
        viewModel.loadProfileCompletion()
        viewModel.loadMedicalRecords()
    }

    // Populate form when records load
    LaunchedEffect(medicalRecords) {
        if (medicalRecords != null && !recordsLoaded) {
            allergiesText = medicalRecords?.allergies?.joinToString(", ") ?: ""
            treatmentsText = medicalRecords?.special_treatments?.joinToString(", ") ?: ""
            medicinesText = medicalRecords?.medicines?.joinToString(", ") ?: ""
            medicalHistoryText = medicalRecords?.medical_history ?: ""
            recordsLoaded = true
        }
    }

    // Show caregiver dialog when lookup result arrives
    LaunchedEffect(caregiverLookup) {
        if (caregiverLookup != null) {
            showCaregiverDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ===== Header (matching ProfileScreen / SettingsScreen style) =====
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
                    text = "Health Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DeepBlue
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(ThemeConfig.Padding.Screen),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ===== Profile Completion =====
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ThemeConfig.Radius.XLarge),
                    color = AppColors.CardBackground,
                    shadowElevation = ThemeConfig.Elevation.Medium
                ) {
                    Column(
                        modifier = Modifier.padding(ThemeConfig.Padding.Card + 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Profile Completion",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DeepBlue
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val percentage = profileCompletion?.percentage ?: 0
                        val progressColor = when {
                            percentage >= 80 -> AppColors.SoftGreen
                            percentage >= 50 -> Color(0xFFF59E0B)
                            else -> AppColors.MutedRed
                        }

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { percentage / 100f },
                                modifier = Modifier.size(100.dp),
                                color = progressColor,
                                strokeWidth = 8.dp,
                                trackColor = AppColors.SurfaceVariant
                            )
                            Text(
                                "$percentage%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = progressColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "${profileCompletion?.completed_fields ?: 0}/${profileCompletion?.total_fields ?: 12} fields completed",
                            fontSize = 14.sp,
                            color = AppColors.SecondaryText
                        )

                        val missing = profileCompletion?.missing_fields ?: emptyList()
                        if (missing.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Missing: ${missing.joinToString(", ") { it.replace("_", " ") }}",
                                fontSize = 12.sp,
                                color = Color(0xFFF59E0B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ===== Caregiver Linking =====
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ThemeConfig.Radius.XLarge),
                    color = AppColors.CardBackground,
                    shadowElevation = ThemeConfig.Elevation.Medium
                ) {
                    Column(modifier = Modifier.padding(ThemeConfig.Padding.Card + 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Link Caregiver",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.DeepBlue
                                )
                                Text(
                                    "Connect with your care provider",
                                    fontSize = 13.sp,
                                    color = AppColors.SecondaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Show linked caregiver status
                        val linkedCaregiver = currentUser?.caregiver_id
                        if (!linkedCaregiver.isNullOrEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ThemeConfig.Radius.Medium),
                                color = AppColors.SoftGreen.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AppColors.SoftGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Linked to: $linkedCaregiver",
                                        color = AppColors.SoftGreen,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = caregiverIdInput,
                            onValueChange = { caregiverIdInput = it },
                            label = { Text("Caregiver ID") },
                            placeholder = { Text("Enter caregiver's ID") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ThemeConfig.Radius.Large),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (caregiverIdInput.isNotBlank()) {
                                    scope.launch { viewModel.lookupCaregiver(caregiverIdInput.trim()) }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ThemeConfig.TouchTarget.Minimum),
                            shape = RoundedCornerShape(ThemeConfig.Radius.Large),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                contentColor = Color.White
                            ),
                            enabled = caregiverIdInput.isNotBlank() && !isLoading
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Search Caregiver",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // ===== Medical Records =====
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ThemeConfig.Radius.XLarge),
                    color = AppColors.CardBackground,
                    shadowElevation = ThemeConfig.Elevation.Medium
                ) {
                    Column(modifier = Modifier.padding(ThemeConfig.Padding.Card + 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Medical Records",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.DeepBlue
                                )
                                Text(
                                    "Keep your health information updated",
                                    fontSize = 13.sp,
                                    color = AppColors.SecondaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Allergies
                        OutlinedTextField(
                            value = allergiesText,
                            onValueChange = { allergiesText = it },
                            label = { Text("Allergies") },
                            placeholder = { Text("e.g., Peanuts, Penicillin, Dust") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ThemeConfig.Radius.Large),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Special Treatments
                        OutlinedTextField(
                            value = treatmentsText,
                            onValueChange = { treatmentsText = it },
                            label = { Text("Special Treatments") },
                            placeholder = { Text("e.g., Physiotherapy, Dialysis") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ThemeConfig.Radius.Large),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Medicines
                        OutlinedTextField(
                            value = medicinesText,
                            onValueChange = { medicinesText = it },
                            label = { Text("Current Medicines") },
                            placeholder = { Text("e.g., Aspirin 100mg, Metformin 500mg") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ThemeConfig.Radius.Large),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Medical History
                        OutlinedTextField(
                            value = medicalHistoryText,
                            onValueChange = { medicalHistoryText = it },
                            label = { Text("Medical History") },
                            placeholder = { Text("Brief notes on medical history...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ThemeConfig.Radius.Large),
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Save Button
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.updateMedicalRecords(
                                        allergies = allergiesText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        specialTreatments = treatmentsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        medicines = medicinesText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        medicalHistory = medicalHistoryText
                                    )
                                    viewModel.loadProfileCompletion()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ThemeConfig.TouchTarget.Minimum),
                            shape = RoundedCornerShape(ThemeConfig.ButtonDimensions.CornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                contentColor = Color.White
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Done, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Save Medical Records",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // ===== Status Messages =====
            item {
                errorMessage?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ThemeConfig.Radius.Medium),
                        color = AppColors.MutedRed.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = AppColors.MutedRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = AppColors.MutedRed, fontSize = 14.sp)
                        }
                    }
                }

                successMessage?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ThemeConfig.Radius.Medium),
                        color = AppColors.SoftGreen.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppColors.SoftGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = AppColors.SoftGreen, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // ===== Caregiver Confirmation Dialog =====
    if (showCaregiverDialog && caregiverLookup != null) {
        AlertDialog(
            onDismissRequest = {
                showCaregiverDialog = false
                viewModel.clearCaregiverLookup()
            },
            containerColor = Color.White,
            title = {
                Text(
                    "Confirm Caregiver",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DeepBlue,
                    fontSize = 22.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Do you want to link with this caregiver?",
                        color = AppColors.SecondaryText,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ThemeConfig.Radius.Large),
                        color = AppColors.Primary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            AppColors.Primary.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
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
                                Text(
                                    caregiverLookup?.full_name?.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    caregiverLookup?.full_name ?: "Unknown",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AppColors.DeepBlue
                                )
                                Text(
                                    "ID: ${caregiverLookup?.caregiver_id ?: ""}",
                                    fontSize = 12.sp,
                                    color = AppColors.SecondaryText
                                )
                                if (!caregiverLookup?.mobile_number.isNullOrEmpty()) {
                                    Text(
                                        "Phone: ${caregiverLookup?.mobile_number}",
                                        fontSize = 13.sp,
                                        color = AppColors.SecondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCaregiverDialog = false
                        scope.launch {
                            viewModel.linkToCaregiver(caregiverIdInput.trim())
                            viewModel.loadProfileCompletion()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(ThemeConfig.ButtonDimensions.CornerRadius)
                ) {
                    Text("Confirm & Link", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCaregiverDialog = false
                    viewModel.clearCaregiverLookup()
                }) {
                    Text("Cancel", color = AppColors.SecondaryText)
                }
            },
            shape = RoundedCornerShape(ThemeConfig.Radius.XLarge)
        )
    }
}
