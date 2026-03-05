package com.rp_elderycareapp.components.reminder

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rp_elderycareapp.data.reminder.Reminder
import kotlinx.datetime.Clock

/**
 * Full-screen alarm dialog that displays when a reminder triggers
 * Features:
 * - Pulsing alarm icon animation
 * - Large text for elderly users
 * - Reminder details display
 * - Stop Alarm (with response tracking), Snooze, and Need Help options
 */
@Composable
fun AlarmDialog(
    reminder: Reminder,
    repeatCount: Int = 0,
    onDismiss: () -> Unit,
    onSnooze: (Int) -> Unit,
    onStopAlarm: () -> Unit,  // NEW: Opens response dialog
    onNeedHelp: () -> Unit    // NEW: Request help when confused
) {
    // Pulsing animation for the alarm icon
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEF4444),
                            Color(0xFFDC2626),
                            Color(0xFFB91C1C)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Repeat count badge — only visible on repeated alarms
                if (repeatCount > 0) {
                    Surface(
                        color = Color(0xFFB91C1C),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🔁 REPEAT #$repeatCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }

                // Pulsing alarm icon
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Alarm",
                    tint = Color.White,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )
                
                // "REMINDER" text
                Text(
                    text = "REMINDER",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                
                // Reminder card with details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title
                        Text(
                            text = reminder.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Description
                        if (!reminder.description.isNullOrBlank()) {
                            Text(
                                text = reminder.description,
                                fontSize = 20.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                        
                        // Details row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DetailChip(
                                icon = Icons.Default.Category,
                                text = reminder.category.uppercase(),
                                color = Color(0xFF3B82F6)
                            )
                            DetailChip(
                                icon = Icons.Default.PriorityHigh,
                                text = reminder.priority.uppercase(),
                                color = when (reminder.priority.lowercase()) {
                                    "critical" -> Color(0xFFDC2626)
                                    "high" -> Color(0xFFEF4444)
                                    "medium" -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                }
                            )
                        }
                        
                        // Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reminder.scheduledTime,
                                fontSize = 16.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stop Alarm button (replaces "Mark as Complete")
                    Button(
                        onClick = onStopAlarm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)  // Green
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Stop Alarm",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "✅ STOP ALARM",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Snooze button - 3 minutes
                    Button(
                        onClick = {
                            onSnooze(3)  // 3 minutes snooze
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)  // Blue
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Snooze,
                            contentDescription = "Snooze",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "⏰ SNOOZE (3 MIN)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Need Help link (NOT a button - just text)
                    TextButton(
                        onClick = onNeedHelp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Need Help? Tap here",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFF44336),  // Red
                            textAlign = TextAlign.Center,
                            style = LocalTextStyle.current.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(colors = listOf(color, color)),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
