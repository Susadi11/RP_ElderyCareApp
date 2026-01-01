package com.rp_elderycareapp

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getApiBaseUrl(): String = ApiConfig.BASE_URL_ANDROID_EMULATOR