package com.rp_elderycareapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors

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
    // WasmJS/Web: Google Sign-In not available - show disabled button
    OutlinedButton(
        onClick = { /* Not available on Web */ },
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
        enabled = false
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Google "G" logo
            Box(modifier = Modifier.size(20.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeW = w * 0.18f
                    val inset = strokeW / 2f
                    val arcSize = Size(w - strokeW, h - strokeW)
                    val arcOffset = Offset(inset, inset)

                    drawArc(color = Color(0xFF4285F4), startAngle = -50f, sweepAngle = 160f, useCenter = false, topLeft = arcOffset, size = arcSize, style = Stroke(strokeW, cap = StrokeCap.Butt))
                    drawArc(color = Color(0xFF34A853), startAngle = 80f, sweepAngle = 60f, useCenter = false, topLeft = arcOffset, size = arcSize, style = Stroke(strokeW, cap = StrokeCap.Butt))
                    drawArc(color = Color(0xFFFBBC05), startAngle = 140f, sweepAngle = 60f, useCenter = false, topLeft = arcOffset, size = arcSize, style = Stroke(strokeW, cap = StrokeCap.Butt))
                    drawArc(color = Color(0xFFEA4335), startAngle = 200f, sweepAngle = 110f, useCenter = false, topLeft = arcOffset, size = arcSize, style = Stroke(strokeW, cap = StrokeCap.Butt))
                    drawLine(color = Color(0xFF4285F4), start = Offset(w * 0.5f, h * 0.5f), end = Offset(w - inset, h * 0.5f), strokeWidth = strokeW, cap = StrokeCap.Butt)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sign in with Google (Web not supported)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.25.sp
            )
        }
    }
}
