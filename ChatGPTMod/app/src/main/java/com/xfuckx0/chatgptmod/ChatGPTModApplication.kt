package com.xfuckx0.chatgptmod

import android.app.Application
import com.xfuckx0.chatgptmod.data.ChatRepository
import com.xfuckx0.chatgptmod.network.ApiService
import com.xfuckx0.chatgptmod.network.OpenCodeApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ChatGPTModApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ChatGPTModApplication
            private set
    }

    val apiService: ApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://opencode.ai/inference/openai/v1/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(OpenCodeApi.createClient())
            .build()
            .create(ApiService::class.java)
    }

    val openCodeApi: OpenCodeApi by lazy { OpenCodeApi(apiService) }
    val chatRepository: ChatRepository by lazy { ChatRepository(this) }
}