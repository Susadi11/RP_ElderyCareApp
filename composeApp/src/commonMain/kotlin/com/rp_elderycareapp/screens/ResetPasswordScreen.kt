package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.rp_elderycareapp.viewmodel.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors

// Platform-specific visibility icon
@Composable
expect fun ResetPasswordVisibilityIcon(): Unit

@Composable
fun ResetPasswordScreen(
    authViewModel: AuthViewModel,
    initialEmail: String = "",
    onResetSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf(initialEmail) }
    var resetCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Observe ViewModel state
    val isLoading = authViewModel.isLoading.value
    val errorMessage = authViewModel.errorMessage.value
    val successMessage = authViewModel.successMessage.value
    
    // Navigate on success
    LaunchedEffect(successMessage) {
        if (successMessage != null && successMessage.contains("successful")) {
            kotlinx.coroutines.delay(1500)
            onResetSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.LightLavender,
                        AppColors.LightPeach
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Header icon
            Text(
                text = "🔑",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Title
            Text(
                text = "Reset Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.DeepBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Enter the 6-digit code sent to your email and create a new password.",
                fontSize = 16.sp,
                color = AppColors.SecondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email field (readonly if pre-filled)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("Enter your email") },
                singleLine = true,
                enabled = initialEmail.isEmpty(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                    focusedLabelColor = AppColors.Primary,
                    cursorColor = AppColors.Primary,
                    disabledBorderColor = AppColors.Primary.copy(alpha = 0.2f),
                    disabledLabelColor = AppColors.SecondaryText
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reset code field
            OutlinedTextField(
                value = resetCode,
                onValueChange = {
                    // Only allow digits, max 6
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        resetCode = it
                    }
                    authViewModel.clearError()
                },
                label = { Text("Reset Code") },
                placeholder = { Text("Enter 6-digit code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                    focusedLabelColor = AppColors.Primary,
                    cursorColor = AppColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New password field
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    authViewModel.clearError()
                },
                label = { Text("New Password") },
                placeholder = { Text("Create new password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        ResetPasswordVisibilityIcon()
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                    focusedLabelColor = AppColors.Primary,
                    cursorColor = AppColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    authViewModel.clearError()
                },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter new password") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        ResetPasswordVisibilityIcon()
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Primary.copy(alpha = 0.3f),
                    focusedLabelColor = AppColors.Primary,
                    cursorColor = AppColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFC62828),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success message
            if (successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = successMessage,
                        color = Color(0xFF2E7D32),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reset password button
            Button(
                onClick = {
                    when {
                        email.isEmpty() -> authViewModel.errorMessage.value = "Please enter your email"
                        resetCode.isEmpty() -> authViewModel.errorMessage.value = "Please enter the reset code"
                        resetCode.length != 6 -> authViewModel.errorMessage.value = "Reset code must be 6 digits"
                        newPassword.isEmpty() -> authViewModel.errorMessage.value = "Please enter a new password"
                        newPassword.length < 8 -> authViewModel.errorMessage.value = "Password must be at least 8 characters"
                        confirmPassword.isEmpty() -> authViewModel.errorMessage.value = "Please confirm your password"
                        newPassword != confirmPassword -> authViewModel.errorMessage.value = "Passwords do not match"
                        else -> {
                            scope.launch {
                                authViewModel.resetPassword(
                                    email = email,
                                    resetCode = resetCode,
                                    newPassword = newPassword,
                                    confirmPassword = confirmPassword
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Reset Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
