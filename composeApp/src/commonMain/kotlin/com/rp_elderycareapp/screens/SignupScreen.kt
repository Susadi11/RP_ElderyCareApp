package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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

@Composable
expect fun SignupVisibilityIcon(isVisible: Boolean)

@Composable
expect fun SignupPersonIcon()

@Composable
expect fun SignupEmailIcon()

@Composable
expect fun SignupPhoneIcon()

@Composable
expect fun SignupLockIcon()

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onSignupSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Observe ViewModel state
    val isLoading = authViewModel.isLoading.value
    val errorMessage = authViewModel.errorMessage.value
    val successMessage = authViewModel.successMessage.value
    
    // Navigate on successful registration
    LaunchedEffect(successMessage) {
        if (successMessage != null && successMessage.contains("successful")) {
            kotlinx.coroutines.delay(1500)  // Show success message briefly
            onSignupSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.Background,
                        AppColors.LightBlue.copy(alpha = 0.1f)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Logo
            AppLogo(
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.DeepBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Join us and start your care journey",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Full Name field
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    authViewModel.clearError()
                },
                label = { Text("Full Name") },
                placeholder = { Text("Enter your full name") },
                leadingIcon = { SignupPersonIcon() },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
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

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    authViewModel.clearError()
                },
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                leadingIcon = { SignupEmailIcon() },
                singleLine = true,
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
                    cursorColor = AppColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone field
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    authViewModel.clearError()
                },
                label = { Text("Phone Number") },
                placeholder = { Text("Enter your phone number") },
                leadingIcon = { SignupPhoneIcon() },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
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

            // Age field (optional)
            OutlinedTextField(
                value = age,
                onValueChange = {
                    // Only allow digits
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        age = it
                    }
                },
                label = { Text("Age (Optional)") },
                placeholder = { Text("Enter your age") },
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

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    authViewModel.clearError()
                },
                label = { Text("Password") },
                placeholder = { Text("Create a password") },
                leadingIcon = { SignupLockIcon() },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        SignupVisibilityIcon(passwordVisible)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
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

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    authViewModel.clearError()
                },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter your password") },
                leadingIcon = { SignupLockIcon() },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        SignupVisibilityIcon(confirmPasswordVisible)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Perform validation and signup
                    }
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

            // Terms and conditions checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColors.Primary,
                        uncheckedColor = AppColors.Primary.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text = "I agree to the Terms & Conditions",
                    fontSize = 14.sp,
                    color = AppColors.SecondaryText,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = AppColors.MutedRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign up button
            Button(
                onClick = {
                    when {
                        fullName.isEmpty() -> authViewModel.errorMessage.value = "Please enter your full name"
                        email.isEmpty() -> authViewModel.errorMessage.value = "Please enter your email"
                        phone.isEmpty() -> authViewModel.errorMessage.value = "Please enter your phone number"
                        password.isEmpty() -> authViewModel.errorMessage.value = "Please create a password"
                        password.length < 8 -> authViewModel.errorMessage.value = "Password must be at least 8 characters"
                        confirmPassword.isEmpty() -> authViewModel.errorMessage.value = "Please confirm your password"
                        password != confirmPassword -> authViewModel.errorMessage.value = "Passwords do not match"
                        !agreedToTerms -> authViewModel.errorMessage.value = "Please agree to the Terms & Conditions"
                        else -> {
                            scope.launch {
                                authViewModel.register(
                                    fullName = fullName,
                                    email = email,
                                    phoneNumber = phone,
                                    age = age.toIntOrNull(),
                                    password = password,
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
                        text = "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = AppColors.SecondaryText,
                    fontSize = 16.sp
                )
                Text(
                    text = "Sign In",
                    color = AppColors.Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
