package com.rp_elderycareapp.components.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.data.reminder.Reminder
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ReminderCard(
    reminder: Reminder,
    onSnooze: () -> Unit,
    onComplete: () -> Unit,
    onRespond: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (reminder.priority.lowercase()) {
        "critical" -> Color(0xFFD32F2F)
        "high" -> Color(0xFFF57C00)
        "medium" -> Color(0xFFFBC02D)
        else -> Color(0xFF388E3C)
    }
    
    val categoryIcon = when (reminder.category.lowercase()) {
        "medication" -> Icons.Default.MedicalServices
        "appointment" -> Icons.Default.CalendarToday
        "meal" -> Icons.Default.Restaurant
        "exercise" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Notifications
    }
    
    // Parse scheduled time from backend
    println("🔍 RAW scheduled_time from backend: '${reminder.scheduledTime}'")
    
    val scheduledTime = try {
        // Backend format: "2026-01-08T08:00:00.000+00:00"
        // Remove milliseconds and normalize timezone
        var timeStr = reminder.scheduledTime.trim()
        
        // Remove milliseconds if present (.000, .123, etc)
        timeStr = timeStr.replace(Regex("\\.\\d{3}"), "")
        
        // Handle timezone offset formats
        timeStr = when {
            timeStr.endsWith("+00:00") -> timeStr.replace("+00:00", "Z")
            timeStr.endsWith("+00") -> timeStr.replace("+00", "Z")
            timeStr.endsWith("-00:00") -> timeStr.replace("-00:00", "Z")
            !timeStr.endsWith("Z") && !timeStr.contains("+") && !timeStr.contains("Z", ignoreCase = false) -> "${timeStr}Z"
            else -> timeStr
        }
        
        println("🔍 PARSED scheduled_time: '$timeStr'")
        val instant = Instant.parse(timeStr)
        println("✅ Successfully parsed to: $instant")
        instant
    } catch (e: Exception) {
        println("❌ PARSE FAILED for: '${reminder.scheduledTime}'")
        println("❌ Error: ${e.message}")
        e.printStackTrace()
        Clock.System.now()
    }
    
    val now = Clock.System.now()
    val diffMillis = (scheduledTime - now).inWholeMinutes
    
    println("⏰ Reminder: ${reminder.title}")
    println("   Scheduled instant: $scheduledTime")
    println("   Current time: $now")
    println("   Difference (minutes): $diffMillis")
    
    // Format the scheduled date/time for display
    // Parse as UTC since backend stores in UTC, but we want to display the entered time
    val localDateTime = scheduledTime.toLocalDateTime(TimeZone.UTC)
    val nowDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    
    // Format time in 12-hour format with AM/PM - showing the time as user entered it
    val hour12 = if (localDateTime.hour == 0) 12 
                 else if (localDateTime.hour > 12) localDateTime.hour - 12 
                 else localDateTime.hour
    val amPm = if (localDateTime.hour >= 12) "PM" else "AM"
    val timeString = "${hour12}:${localDateTime.minute.toString().padStart(2, '0')} $amPm"
    
    // Get month name
    val monthName = when (localDateTime.monthNumber) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> localDateTime.monthNumber.toString()
    }
    
    // ALWAYS show the full date and time as entered
    val scheduledDateText = "$monthName ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    val scheduledTimeText = "$scheduledDateText at $timeString"
    
    println("📅 Display: $scheduledDateText")
    println("⏰ Display time: $timeString")
    
    // Status indicator
    val statusText = when {
        diffMillis < -5 -> "Overdue"
        diffMillis <= 5 -> "Due Now"
        diffMillis < 60 -> "In $diffMillis min"
        else -> null
    }
    
    val isOverdue = diffMillis < 0
    val isDueNow = diffMillis >= -5 && diffMillis <= 5
    
    println("   Display: $scheduledTimeText ${statusText?.let { "($it)" } ?: ""}")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with priority indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = reminder.category,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = reminder.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(priorityColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, priorityColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reminder.priority.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Description
            if (!reminder.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Scheduled Date & Time Display
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Date and Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = scheduledDateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = when {
                            isOverdue -> Color(0xFFD32F2F)
                            isDueNow -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Show status badge if overdue or due now
                    if (statusText != null) {
                        Box(
                            modifier = Modifier
                                .background(
                                    when {
                                        isOverdue -> Color(0xFFD32F2F).copy(alpha = 0.1f)
                                        isDueNow -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isOverdue -> Color(0xFFD32F2F)
                                    isDueNow -> Color(0xFF4CAF50)
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snooze")
                }
                
                Button(
                    onClick = onRespond,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Respond",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Respond")
                }
            }
        }
    }
}
