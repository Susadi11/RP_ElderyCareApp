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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rp_elderycareapp.services.AudioRecorder
import kotlinx.coroutines.launch

/**
 * Dialog for recording voice reminders
 * Features:
 * - Permission handling
 * - Record/Stop controls
 * - Playback preview
 * - Visual recording indicator
 * - Duration display
 */
@Composable
fun AudioRecorderDialog(
    audioRecorder: AudioRecorder,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    userId: String
) {
    val scope = rememberCoroutineScope()
    val isRecording by audioRecorder.isRecording.collectAsState()
    val audioFilePath by audioRecorder.audioFilePath.collectAsState()
    val duration by audioRecorder.recordingDuration.collectAsState()
    
    var hasPermission by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Check permission on mount
    LaunchedEffect(Unit) {
        hasPermission = audioRecorder.hasPermission()
    }
    
    // Cleanup on dismiss
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.cleanup()
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isRecording,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Recording",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "Voice Reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Record your reminder instructions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Divider()
                
                if (!hasPermission) {
                    // Permission request UI
                    PermissionRequestUI(
                        onRequestPermission = {
                            scope.launch {
                                hasPermission = audioRecorder.requestPermission()
                            }
                        }
                    )
                } else {
                    // Recording UI
                    RecordingControlsUI(
                        isRecording = isRecording,
                        duration = duration,
                        hasRecording = audioFilePath != null,
                        isPlaying = isPlaying,
                        isSubmitting = isSubmitting,
                        onStartRecording = {
                            scope.launch {
                                audioRecorder.startRecording()
                                    .onFailure { error ->
                                        errorMessage = error.message ?: "Failed to start recording"
                                        showError = true
                                    }
                            }
                        },
                        onStopRecording = {
                            scope.launch {
                                audioRecorder.stopRecording()
                                    .onFailure { error ->
                                        errorMessage = error.message ?: "Failed to stop recording"
                                        showError = true
                                    }
                            }
                        },
                        onPlayRecording = {
                            scope.launch {
                                isPlaying = true
                                audioRecorder.playRecording()
                                    .onSuccess {
                                        // Auto-stop after playback
                                        kotlinx.coroutines.delay(duration * 1000)
                                        isPlaying = false
                                    }
                                    .onFailure { error ->
                                        isPlaying = false
                                        errorMessage = error.message ?: "Failed to play recording"
                                        showError = true
                                    }
                            }
                        },
                        onStopPlayback = {
                            scope.launch {
                                audioRecorder.stopPlayback()
                                isPlaying = false
                            }
                        },
                        onDeleteRecording = {
                            audioRecorder.deleteRecording()
                        },
                        onSubmit = {
                            if (audioFilePath != null) {
                                isSubmitting = true
                                onSubmit(audioFilePath!!)
                            }
                        }
                    )
                }
                
                // Error message
                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isRecording && !isSubmitting) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestUI(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MicOff,
            contentDescription = "Permission Required",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Text(
            text = "Microphone Permission Required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Please grant microphone permission to record voice reminders",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grant Permission")
        }
    }
}

@Composable
private fun RecordingControlsUI(
    isRecording: Boolean,
    duration: Long,
    hasRecording: Boolean,
    isPlaying: Boolean,
    isSubmitting: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayRecording: () -> Unit,
    onStopPlayback: () -> Unit,
    onDeleteRecording: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Recording indicator or status
        if (isRecording) {
            RecordingIndicator(duration = duration)
        } else if (hasRecording) {
            RecordingCompleteIndicator(duration = duration)
        } else {
            ReadyToRecordIndicator()
        }
        
        // Main action button (Record/Stop)
        if (isRecording) {
            Button(
                onClick = onStopRecording,
                modifier = Modifier
                    .size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Stop Recording",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        } else {
            Button(
                onClick = onStartRecording,
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                enabled = !hasRecording && !isSubmitting
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Start Recording",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        // Playback and submit controls (shown after recording)
        if (hasRecording && !isRecording) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play button
                OutlinedButton(
                    onClick = if (isPlaying) onStopPlayback else onPlayRecording,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPlaying) "Stop" else "Play")
                }
                
                // Delete button
                OutlinedButton(
                    onClick = onDeleteRecording,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !isSubmitting
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
            
            // Submit button
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating Reminder...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Reminder")
                }
            }
        }
    }
}

@Composable
private fun RecordingIndicator(duration: Long) {
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.Red.copy(alpha = alpha), CircleShape)
        )
        
        Text(
            text = "Recording...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Red
        )
        
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RecordingCompleteIndicator(duration: Long) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Recording Complete",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(32.dp)
        )
        
        Text(
            text = "Recording Complete",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReadyToRecordIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = "Ready",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        
        Text(
            text = "Ready to Record",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "Tap the button below to start",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
