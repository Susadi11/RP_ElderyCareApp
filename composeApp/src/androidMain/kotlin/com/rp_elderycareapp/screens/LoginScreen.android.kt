package com.rp_elderycareapp.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rp_elderycareapp.ui.theme.AppColors
import kotlinx.coroutines.launch

private const val GOOGLE_WEB_CLIENT_ID = "717354778892-h5t7l51m79s1b61dmsvmmeqce2i1frr0.apps.googleusercontent.com"

@Composable
actual fun LoginVisibilityIcon(isVisible: Boolean) {
    Icon(
        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        contentDescription = if (isVisible) "Hide password" else "Show password",
        tint = AppColors.SecondaryText,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun LoginEmailIcon() {
    Icon(
        imageVector = Icons.Default.Email,
        contentDescription = "Email",
        tint = AppColors.Primary,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun LoginLockIcon() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Password",
        tint = AppColors.Primary,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun GoogleSignInButton(onIdTokenReceived: (String) -> Unit, isLoading: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val credentialManager = remember { CredentialManager.create(context) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                scope.launch {
                    isSigningIn = true
                    errorMessage = null
                    
                    try {
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(false)
                            .setAutoSelectEnabled(false)
                            .build()
                        
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        
                        val credential = result.credential
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        
                        Log.d("GoogleSignIn", "Successfully obtained ID token")
                        onIdTokenReceived(idToken)
                        
                    } catch (e: GetCredentialException) {
                        Log.e("GoogleSignIn", "GetCredentialException: ${e.message}", e)
                        errorMessage = when {
                            e.message?.contains("cancelled", ignoreCase = true) == true -> null // User cancelled
                            e.message?.contains("No credentials available", ignoreCase = true) == true -> 
                                "No Google account found. Please add a Google account to your device."
                            else -> "Google Sign-In failed: ${e.message}"
                        }
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Unexpected error: ${e.message}", e)
                        errorMessage = "Google Sign-In failed: ${e.message}"
                    } finally {
                        isSigningIn = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF3C4043),
                disabledContainerColor = Color(0xFFF8F9FA),
                disabledContentColor = Color(0xFF80868B)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
            enabled = !isLoading && !isSigningIn
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF4285F4),
                        strokeWidth = 2.dp
                    )
                } else {
                    // Google "G" logo using Canvas
                    Box(modifier = Modifier.size(20.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val strokeW = w * 0.18f
                            val inset = strokeW / 2f
                            val arcSize = Size(w - strokeW, h - strokeW)
                            val arcOffset = Offset(inset, inset)

                            // Blue arc
                            drawArc(
                                color = Color(0xFF4285F4),
                                startAngle = -50f,
                                sweepAngle = 160f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(strokeW, cap = StrokeCap.Butt)
                            )

                            // Green arc
                            drawArc(
                                color = Color(0xFF34A853),
                                startAngle = 80f,
                                sweepAngle = 60f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(strokeW, cap = StrokeCap.Butt)
                            )

                            // Yellow arc
                            drawArc(
                                color = Color(0xFFFBBC05),
                                startAngle = 140f,
                                sweepAngle = 60f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(strokeW, cap = StrokeCap.Butt)
                            )

                            // Red arc
                            drawArc(
                                color = Color(0xFFEA4335),
                                startAngle = 200f,
                                sweepAngle = 110f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(strokeW, cap = StrokeCap.Butt)
                            )

                            // Blue horizontal bar
                            drawLine(
                                color = Color(0xFF4285F4),
                                start = Offset(w * 0.5f, h * 0.5f),
                                end = Offset(w - inset, h * 0.5f),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Butt
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isSigningIn) "Signing in..." else "Sign in with Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.25.sp
                )
            }
        }
        
        // Show error message if any
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = AppColors.MutedRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
