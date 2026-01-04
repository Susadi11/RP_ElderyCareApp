package com.rp_elderycareapp.platform

import android.speech.tts.TextToSpeech as AndroidTextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
actual fun rememberTextToSpeech(): TextToSpeech {
    val context = LocalContext.current
    val tts = remember {
        object : TextToSpeech {
            private var androidTts: AndroidTextToSpeech? = null

            init {
                androidTts = AndroidTextToSpeech(context) { status ->
                    if (status == AndroidTextToSpeech.SUCCESS) {
                        androidTts?.language = Locale.US
                    }
                }
            }

            override fun speak(text: String) {
                androidTts?.speak(text, AndroidTextToSpeech.QUEUE_FLUSH, null, null)
            }

            override fun shutdown() {
                androidTts?.shutdown()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
        }
    }

    return tts
}
