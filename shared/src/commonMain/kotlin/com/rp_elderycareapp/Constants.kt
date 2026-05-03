package com.rp_elderycareapp

const val SERVER_PORT = 8080

object ApiConfig {
    // Backend API port
    const val BACKEND_PORT = 8080

    // Base URLs for different platforms
    // For Android emulator: use 10.0.2.2 instead of localhost
    // For iOS simulator and other platforms: use localhost

    // ===== LOCALHOST (uncomment for local development) =====
     const val BASE_URL_LOCALHOST = "http://localhost:8080"
     const val BASE_URL_ANDROID_EMULATOR = "http://10.0.2.2:8080"

    // ===== HOSTED (uncomment for production) =====
//    const val BASE_URL_LOCALHOST = "https://api.hale-eldery.life"
//    const val BASE_URL_ANDROID_EMULATOR = "https://api.hale-eldery.life"

    // API Endpoints
    object Endpoints {
        
        const val CHAT_TEXT = "/chat/text"
        const val CHAT_VOICE = "/chat/voice"
        const val CHAT_SESSIONS = "/chat/sessions"
        const val CHAT_HEALTH = "/chat/health"

        const val REMINDERS = "/api/reminders"
        
        // User Authentication
        const val USER_REGISTER = "/api/user/register"
        const val USER_LOGIN = "/api/user/login"
        const val USER_PROFILE = "/api/user/profile"
        const val USER_FORGOT_PASSWORD = "/api/user/forgot-password"
        const val USER_RESET_PASSWORD = "/api/user/reset-password"
        const val USER_REFRESH_TOKEN = "/api/user/refresh-token"
    }
}