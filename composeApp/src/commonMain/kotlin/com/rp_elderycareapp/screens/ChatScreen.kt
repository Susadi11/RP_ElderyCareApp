package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.rp_elderycareapp.components.AIAvatarIcon
import com.rp_elderycareapp.components.ChatInputBar
import com.rp_elderycareapp.components.MessageBubble
import com.rp_elderycareapp.data.MessageType
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.viewmodel.AuthViewModel
import com.rp_elderycareapp.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    authViewModel: AuthViewModel? = null,
    chatViewModel: ChatViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var currentMessage by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }

    val messages by chatViewModel.messages
    val isTyping by chatViewModel.isTyping
    val errorMessage by chatViewModel.errorMessage

    val currentUser = authViewModel?.currentUser?.value
    val userId = currentUser?.user_id ?: "guest_user"

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    InitializeVoiceRecorderIfNeeded()

    LaunchedEffect(messages.size, isTyping) {
        val totalItems = messages.size + if (isTyping) 1 else 0
        if (totalItems > 0) {
            coroutineScope.launch {
                try {
                    delay(100)
                    listState.animateScrollToItem(totalItems - 1)
                } catch (e: Exception) {
                    listState.scrollToItem(totalItems - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ChatHeaderContent(
            isTyping = isTyping,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    onVoicePlayPause = { messageId ->
                        chatViewModel.toggleVoicePlayback(messageId)
                    }
                )
            }
            if (isTyping) {
                item(key = "typing_indicator") {
                    TypingIndicator()
                }
            }
        }

        ChatInputBar(
            message = currentMessage,
            onMessageChange = { currentMessage = it },
            onSendMessage = {
                if (currentMessage.trim().isNotEmpty()) {
                    val messageText = currentMessage.trim()
                    currentMessage = ""
                    chatViewModel.sendTextMessage(userId, messageText)
                }
            },
            onStartVoiceRecording = {
                startVoiceRecording(
                    onStarted = { isRecording = true },
                    onError = { error ->
                        isRecording = false
                        chatViewModel.errorMessage.value = error
                    }
                )
            },
            onStopVoiceRecording = {
                stopVoiceRecording(
                    onStopped = { audioFilePath ->
                        isRecording = false
                        if (audioFilePath != null) {
                            chatViewModel.sendVoiceMessage(userId, audioFilePath)
                        }
                    }
                )
            },
            onCancelVoiceRecording = {
                cancelVoiceRecording(
                    onCancelled = {
                        isRecording = false
                    }
                )
            },
            isRecording = isRecording
        )
    }
}

@Composable
expect fun ChatHeaderContent(
    isTyping: Boolean,
    onNavigateBack: () -> Unit
)

@Composable
expect fun InitializeVoiceRecorderIfNeeded()

expect fun startVoiceRecording(
    onStarted: () -> Unit,
    onError: (String) -> Unit
)

expect fun stopVoiceRecording(
    onStopped: (String?) -> Unit
)

expect fun cancelVoiceRecording(
    onCancelled: () -> Unit
)

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    // Three dots staggered 200 ms apart within a 1200 ms cycle
    val alphas = listOf(0, 200, 400).map { delayMs ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0.3f at 0
                    0.3f at delayMs
                    1f at (delayMs + 200)
                    0.3f at (delayMs + 400)
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "dot_$delayMs"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AIAvatarIcon()
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = 4.dp, bottomEnd = 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                alphas.forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha.value)
                            )
                    )
                }
            }
        }
    }
}
