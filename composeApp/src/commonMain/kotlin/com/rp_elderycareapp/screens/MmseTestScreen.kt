package com.rp_elderycareapp.screens

import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.data.repository.MmseRepository
import com.rp_elderycareapp.utils.SpeechHelper
import com.rp_elderycareapp.utils.TtsHelper

@Composable
fun MmseTestScreen() {
    // ---------------------- Context & Helpers ----------------------
    val context = LocalContext.current
    val activity = context as Activity

    val tts = remember { TtsHelper(context) }

    // Launcher for Android's speech recognition intent
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val recognized = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (recognized != null) {
            onSpeechRecognized(recognized)
        }
    }

    // ---------------------- State Variables ----------------------
    val questions = MmseRepository.questions
    var index by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf("") }
    var lastRecognized by remember { mutableStateOf("") }

    // Store recognized speech and validation
    var recognizedText by remember { mutableStateOf("") }

    // Recreate SpeechHelper for this screen
    val speechHelper = remember {
        SpeechHelper(activity, launcher) { recognized ->
            recognizedText = recognized
        }
    }

    // ---------------------- UI Layout ----------------------
    val currentQuestion = questions[index]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MMSE Voice Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Question ${index + 1} of ${questions.size}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentQuestion.prompt,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { tts.speak(currentQuestion.prompt) }) {
                Text("🔊 Hear Question")
            }

            Button(onClick = { speechHelper.startListening() }) {
                Text("🎤 Answer")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Show recognized text (for debugging or elderly feedback)
        if (recognizedText.isNotEmpty()) {
            Text(
                text = "You said: \"$recognizedText\"",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Validate Button
        Button(onClick = {
            val normalized = recognizedText.lowercase().trim()
            val correct = currentQuestion.correctAnswers.any { normalized.contains(it) }

            if (correct) {
                score += currentQuestion.points
                feedback = "✅ Correct!"
                tts.speak("That is correct")
            } else {
                feedback = "❌ Wrong"
                tts.speak("That is wrong")
            }

            lastRecognized = recognizedText
            recognizedText = ""
        }) {
            Text("Check Answer ✅")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = feedback, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(40.dp))

        // Next Question Button
        Button(onClick = {
            if (index < questions.lastIndex) {
                index++
                feedback = ""
                tts.speak("Next question")
            } else {
                feedback = "Final Score: $score"
                tts.speak("Your total score is $score out of thirty")
            }
        }) {
            Text(if (index < questions.lastIndex) "Next ➡️" else "Finish 🏁")
        }
    }

    // Clean up TTS on screen destroy
    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }
}

// ---------------------- Helper function ----------------------
private var onSpeechRecognized: (String) -> Unit = {}
fun setSpeechCallback(callback: (String) -> Unit) {
    onSpeechRecognized = callback
}