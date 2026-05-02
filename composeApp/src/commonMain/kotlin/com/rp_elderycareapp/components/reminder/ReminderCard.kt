package com.rp_elderycareapp.components.reminder

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (reminder.priority.lowercase()) {
        "critical" -> Color(0xFFEF4444)
        "high"     -> Color(0xFFF97316)
        "medium"   -> Color(0xFFFACC15)
        else       -> Color(0xFF22C55E)
    }

    val (categoryIcon, categoryColor) = when (reminder.category.lowercase()) {
        "medication"  -> Icons.Default.MedicalServices to Color(0xFF6366F1)
        "appointment" -> Icons.Default.CalendarToday   to Color(0xFF0EA5E9)
        "meal"        -> Icons.Default.Restaurant      to Color(0xFFF97316)
        "exercise"    -> Icons.Default.FitnessCenter   to Color(0xFF22C55E)
        "social"      -> Icons.Default.People          to Color(0xFFEC4899)
        else          -> Icons.Default.Notifications   to Color(0xFF8B5CF6)
    }

    println("🔍 RAW scheduled_time for '${reminder.title}': '${reminder.scheduledTime}'")
    println("🔍 Reminder ID: ${reminder.id}, Status: ${reminder.status}")

    val scheduledTime = try {
        var timeStr = reminder.scheduledTime.trim()
        timeStr = timeStr.replace(Regex("\\.\\d+"), "")
        timeStr = when {
            timeStr.endsWith("+00:00") -> timeStr.replace("+00:00", "Z")
            timeStr.endsWith("+00")    -> timeStr.replace("+00", "Z")
            timeStr.endsWith("-00:00") -> timeStr.replace("-00:00", "Z")
            !timeStr.endsWith("Z") && !timeStr.contains("+") && !timeStr.contains("Z", ignoreCase = false) -> "${timeStr}Z"
            else -> timeStr
        }
        println("🔍 PARSED scheduled_time: '$timeStr'")
        val instant = Instant.parse(timeStr)
        println("✅ Successfully parsed to: $instant")
        instant
    } catch (e: Exception) {
        println("❌ PARSE FAILED for: '${reminder.scheduledTime}' — ${e.message}")
        Clock.System.now()
    }

    val now = Clock.System.now()
    val diffMillis = (scheduledTime - now).inWholeMinutes

    val localDateTime = scheduledTime.toLocalDateTime(TimeZone.currentSystemDefault())

    val hour12 = if (localDateTime.hour == 0) 12
                 else if (localDateTime.hour > 12) localDateTime.hour - 12
                 else localDateTime.hour
    val amPm = if (localDateTime.hour >= 12) "PM" else "AM"
    val timeString = "${hour12}:${localDateTime.minute.toString().padStart(2, '0')} $amPm"

    val monthName = when (localDateTime.monthNumber) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> localDateTime.monthNumber.toString()
    }
    val scheduledDateText = "$monthName ${localDateTime.dayOfMonth}, ${localDateTime.year}"

    val isSnoozed = reminder.status.lowercase() == "snoozed"
    val isOverdue = diffMillis < 0 && !isSnoozed
    val isDueNow  = diffMillis >= -5 && diffMillis <= 5

    val statusText = when {
        isSnoozed && diffMillis < 60 -> "Snoozed (${diffMillis}m)"
        isSnoozed    -> "Snoozed"
        isOverdue    -> "Overdue"
        isDueNow     -> "Due Now"
        diffMillis < 60 -> "In ${diffMillis}m"
        else         -> null
    }
    val statusColor = when {
        isSnoozed -> Color(0xFF0EA5E9)
        isOverdue -> Color(0xFFEF4444)
        isDueNow  -> Color(0xFF22C55E)
        else      -> Color(0xFFF97316)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left priority accent strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top row: category chip | status badge + actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(categoryColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = reminder.category.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 13.sp,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (statusText != null) {
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (onEdit != null) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = reminder.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!reminder.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.description,
                        fontSize = 15.sp,
                        color = Color(0xFF64748B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom row: date + time | priority badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF7BA8C4),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = scheduledDateText,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isOverdue) Color(0xFFEF4444) else Color(0xFF7BA8C4),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = timeString,
                                fontSize = 14.sp,
                                color = if (isOverdue) Color(0xFFEF4444) else Color(0xFF1A1A2E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = reminder.priority.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
