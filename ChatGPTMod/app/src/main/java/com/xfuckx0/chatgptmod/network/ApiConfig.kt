package com.xfuckx0.chatgptmod.network

import android.content.Context

/**
 * Use a key issued to the user by OpenCode Zen. Never ship a shared secret in an APK.
 * Android private preferences are used here; for high-security deployments use a backend.
 */
object ApiConfig {
    private lateinit var appContext: Context
    val freeModels = listOf(
        "space-bunny-free",
        "longcat-2.5-preview-free",
        "mimo-v2.5-free",
        "ling-3.0-flash-fin-free"
    )

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val prefs get() = appContext.getSharedPreferences("zen_api_settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) { prefs.edit().putString("api_key", value.trim()).apply() }

    var model: String
        get() = prefs.getString("model", freeModels.first()) ?: freeModels.first()
        set(value) {
            if (value in freeModels) prefs.edit().putString("model", value).apply()
        }
}
