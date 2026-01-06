package com.rp_elderycareapp

const val SERVER_PORT = 8080

object ApiConfig {
    // Backend API port
    const val BACKEND_PORT = 8000

    // Base URLs for different platforms
    // For Android emulator: use 10.0.2.2 instead of localhost
    // For iOS simulator and other platforms: use localhost
    const val BASE_URL_LOCALHOST = "http://localhost:$BACKEND_PORT"
    const val BASE_URL_ANDROID_EMULATOR = "http://10.0.2.2:$BACKEND_PORT"

    // API Endpoints
    object Endpoints {
        const val CHAT_TEXT = "/chat/text"
        const val CHAT_VOICE = "/chat/voice"
        const val CHAT_SESSIONS = "/chat/sessions"
        const val CHAT_HEALTH = "/chat/health"

        const val REMINDERS = "/api/reminders"
    }
}