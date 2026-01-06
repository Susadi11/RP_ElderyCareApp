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
    
    // Calculate countdown
    val scheduledTime = try {
        Instant.parse(reminder.scheduledTime)
    } catch (e: Exception) {
        println("❌ Failed to parse scheduled time: ${reminder.scheduledTime}")
        e.printStackTrace()
        Clock.System.now()
    }
    val now = Clock.System.now()
    val diffMillis = (scheduledTime - now).inWholeMinutes
    
    // Debug logging
    println("⏰ Reminder: ${reminder.title}")
    println("   Scheduled: ${reminder.scheduledTime}")
    println("   Now: ${now}")
    println("   Diff (minutes): $diffMillis")
    
    // Format the scheduled date/time for display
    val localDateTime = scheduledTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val nowDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    
    val dateTimeText = "${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')} " +
            "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    
    // Check if it's today
    val isToday = localDateTime.date == nowDateTime.date
    val isTomorrow = localDateTime.date.dayOfYear == nowDateTime.date.dayOfYear + 1 && 
                     localDateTime.year == nowDateTime.year
    
    val countdownText = when {
        diffMillis < -60 -> "Overdue: $dateTimeText"
        diffMillis < 0 -> "Overdue (${-diffMillis} min ago)"
        diffMillis == 0L -> "Now!"
        diffMillis <= 1 -> "In 1 minute"
        diffMillis < 60 -> "In $diffMillis minutes"
        isToday -> "Today at ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
        isTomorrow -> "Tomorrow at ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
        else -> dateTimeText
    }
    
    println("   Display: $countdownText")
    
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
            
            // Countdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = if (diffMillis < 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = countdownText,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (diffMillis < 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
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
