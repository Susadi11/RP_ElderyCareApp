package com.rp_elderycareapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.data.MmseQuestion
import com.rp_elderycareapp.data.MmseQuestions
import com.rp_elderycareapp.platform.loadImageResource
import com.rp_elderycareapp.platform.rememberTextToSpeech
import kotlinx.coroutines.launch

enum class RecordingState {
    IDLE,
    LISTENING,
    RECORDED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MmseQuestionsScreen(
    onNavigateBack: () -> Unit = {},
    onTalkWithUs: () -> Unit = {},
    onComplete: (totalScore: Int) -> Unit = {}
) {
    val questions = remember { MmseQuestions.allQuestions }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var recordingState by remember { mutableStateOf(RecordingState.IDLE) }
    var recordedAnswer by remember { mutableStateOf("") }
    var currentScore by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    val textToSpeech = rememberTextToSpeech()

    val offsetY = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
    }

    val currentQuestion = questions[currentQuestionIndex]

    val playQuestionAudio = {
        val textToSpeak = when (currentQuestionIndex) {
            13, 14 -> "What is this called?"
            else -> currentQuestion.question
        }
        textToSpeech.speak(textToSpeak)
    }

    LaunchedEffect(currentQuestionIndex) {
        playQuestionAudio()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F4F8),
                        Color(0xFFF5F9FB)
                    )
                )
            ),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .offset(y = offsetY.value.dp)
                .graphicsLayer { this.alpha = alpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuestionHeader(
                currentQuestion = currentQuestionIndex + 1,
                totalQuestions = questions.size,
                category = currentQuestion.category
            )

            Spacer(modifier = Modifier.height(24.dp))

            QuestionCardWithImage(
                question = currentQuestion,
                questionIndex = currentQuestionIndex,
                recordedAnswer = if (recordingState == RecordingState.RECORDED) recordedAnswer else null,
                onPlayAudio = playQuestionAudio
            )

            Spacer(modifier = Modifier.height(32.dp))

            MicrophoneButton(
                recordingState = recordingState,
                onClick = {
                    when (recordingState) {
                        RecordingState.IDLE -> {
                            recordingState = RecordingState.LISTENING
                            // TODO: Start actual voice recording
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(2000)
                                recordedAnswer = "watch" // Replace with actual transcription
                                recordingState = RecordingState.RECORDED
                            }
                        }
                        RecordingState.LISTENING -> {
                            recordingState = RecordingState.RECORDED
                            recordedAnswer = "watch"
                        }
                        RecordingState.RECORDED -> {
                            recordingState = RecordingState.LISTENING
                            recordedAnswer = ""
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (recordingState == RecordingState.RECORDED) {
                SubmitAnswerButton(
                    onClick = {
                        val pointsEarned = 1 // TODO: Calculate actual score
                        currentScore += pointsEarned

                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            recordingState = RecordingState.IDLE
                            recordedAnswer = ""
                        } else {
                            onComplete(currentScore)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            RepeatQuestionButton(
                onClick = playQuestionAudio
            )

            Spacer(modifier = Modifier.height(24.dp))

            TalkWithUsButton(onClick = onTalkWithUs)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuestionHeader(
    currentQuestion: Int,
    totalQuestions: Int,
    category: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Question $currentQuestion",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280)
                )
            }
            Text(
                text = "$currentQuestion/$totalQuestions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentQuestion.toFloat() / totalQuestions.toFloat())
                    .height(8.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4A9FFF),
                                Color(0xFF3B82F6)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun QuestionCardWithImage(
    question: MmseQuestion,
    questionIndex: Int,
    recordedAnswer: String?,
    onPlayAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (questionIndex != 13 && questionIndex != 14) {
                Text(
                    text = question.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            when (questionIndex) {
                13 -> { // Question 14 - Wristwatch
                    ObjectImageDisplay(
                        imageType = "watch",
                        label = "Wristwatch",
                        onPlayAudio = onPlayAudio
                    )
                }
                14 -> { // Question 15 - Pencil
                    ObjectImageDisplay(
                        imageType = "pencil",
                        label = "Pencil",
                        onPlayAudio = onPlayAudio
                    )
                }
                else -> {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play Question",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            if (recordedAnswer != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD1FAE5)
                    )
                ) {
                    Text(
                        text = "\"$recordedAnswer\"",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF065F46),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ObjectImageDisplay(
    imageType: String,
    label: String,
    onPlayAudio: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onPlayAudio,
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                )
                .background(
                    color = Color(0xFFE3F2FD),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Play Question",
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .size(260.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .border(
                        width = 4.dp,
                        color = Color(0xFF4A9FFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = loadImageResource(imageType),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "What is this called?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MicrophoneButton(
    recordingState: RecordingState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (recordingState == RecordingState.LISTENING) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val buttonColor = when (recordingState) {
        RecordingState.IDLE -> Color(0xFF10B981)
        RecordingState.LISTENING -> Color(0xFFEF4444)
        RecordingState.RECORDED -> Color(0xFF10B981)
    }

    val buttonBackground = when (recordingState) {
        RecordingState.IDLE -> Color(0xFFD1FAE5)
        RecordingState.LISTENING -> Color(0xFFFEE2E2)
        RecordingState.RECORDED -> Color(0xFFD1FAE5)
    }

    val statusText = when (recordingState) {
        RecordingState.IDLE -> "Tap to Answer"
        RecordingState.LISTENING -> "Listening..."
        RecordingState.RECORDED -> "Tap to Re-record"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = buttonColor.copy(alpha = 0.4f)
                )
                .background(
                    color = buttonBackground,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Microphone",
                modifier = Modifier.size(56.dp),
                tint = buttonColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun SubmitAnswerButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFF4A9FFF).copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4A9FFF),
                            Color(0xFF3B82F6)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Submit Answer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RepeatQuestionButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1A1A2E)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = Color(0xFFE5E7EB)
        )
    ) {
        Text(
            text = "Repeat Question",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TalkWithUsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = "Support",
                tint = Color(0xFF4ECCA3),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Talk with Us",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}