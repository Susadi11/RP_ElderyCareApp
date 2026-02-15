package com.rp_elderycareapp.navigation

enum class NavRoutes(val route: String) {
    LOGIN("login"),
    SIGNUP("signup"),
    FORGOT_PASSWORD("forgot_password"),
    RESET_PASSWORD("reset_password"),
    HOME("home"),
    CHAT("chat"),
    GAME("game"),
    MMSE_TEST("mmse_test"),

    MMSE_QUESTIONS("mmse_questions"),
    MMSE_START_TEST("mmse_start_test"),
    REMINDER("reminder"),
    PATIENT_DASHBOARD("patient_dashboard"),
    CAREGIVER_ALERTS("caregiver_alerts"),
    SETTINGS("settings"),
    PROFILE("profile")
}
