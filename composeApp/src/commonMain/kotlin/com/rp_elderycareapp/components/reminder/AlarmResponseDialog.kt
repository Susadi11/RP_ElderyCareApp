package com.rp_elderycareapp.components.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rp_elderycareapp.data.reminder.Reminder
import androidx.compose.foundation.layout.ExperimentalLayoutApi

/**
 * Response popup dialog shown after clicking "Stop Alarm"
 * Collects user's response for cognitive tracking and dementia detection
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmResponseDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var userResponse by remember { mutableStateOf("") }

    // Quick response chips
    val quickResponses = listOf(
        "Done",
        "I did it",
        "Finished",
        "I took it",
        "Completed"
    )

    Dialog(
        onDismissRequest = {
            // Submit even if empty when user tries to dismiss
            onSubmit(userResponse)
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title with emoji
                Text(
                    text = "📝 Tell us what you did",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = "This helps us track your progress",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Text input
                OutlinedTextField(
                    value = userResponse,
                    onValueChange = { userResponse = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 120.dp),
                    placeholder = {
                        Text(
                            "Example: I took my blue pill with water",
                            fontSize = 16.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    maxLines = 5,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        cursorColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick response chips
                Text(
                    text = "Quick Responses:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickResponses.forEach { response ->
                        SuggestionChip(
                            onClick = { userResponse = response },
                            label = {
                                Text(
                                    response,
                                    fontSize = 16.sp
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFE3F2FD),
                                labelColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Submit button (NO BACK BUTTON!)
                Button(
                    onClick = {
                        onSubmit(userResponse)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Submit",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SUBMIT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Info text
                Text(
                    text = "You can leave this blank if you prefer",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
