package com.rp_elderycareapp

import android.annotation.SuppressLint
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@SuppressLint("StaticFieldLeak")
private var preferencesManagerInstance: PreferencesManager? = null

fun initializePreferencesManager(preferencesManager: PreferencesManager) {
    preferencesManagerInstance = preferencesManager
}

actual fun getApiBaseUrl(): String {
    val customUrl = preferencesManagerInstance?.getBaseUrl()
    return customUrl ?: ApiConfig.BASE_URL_ANDROID_EMULATOR
}