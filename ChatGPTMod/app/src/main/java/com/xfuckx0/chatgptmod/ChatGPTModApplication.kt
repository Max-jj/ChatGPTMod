package com.xfuckx0.chatgptmod

import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxjava3.RxDataStore
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import com.xfuckx0.chatgptmod.data.ChatRepository
import com.xfuckx0.chatgptmod.network.ApiService
import com.xfuckx0.chatgptmod.network.OpenCodeApi
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

class ChatGPTModApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeDatabase()
    }

    companion object {
        @Suppress("UNUSED_PARAMETER")
        lateinit var instance: ChatGPTModApplication
            private set

        fun getInstance(): ChatGPTModApplication = instance
    }

    // DataStore for preferences
    private val Context.dataStore: RxDataStore<Preferences> by preferencesDataStore("chatgptmod_prefs")

    val dataStore: RxDataStore<Preferences> by lazy { dataStore }

    // API Service
    val apiService: ApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://opencode.ai/inference/openai/v1/")
            .addConverterFactory(kotlinx.serialization.json.Json.asConverterFactory(json))
            .build()
        retrofit.create(ApiService::class.java)
    }

    // OpenCode API wrapper
    val openCodeApi: OpenCodeApi by lazy { OpenCodeApi(apiService) }

    // Repository
    val chatRepository: ChatRepository by lazy { ChatRepository(this) }

    private fun initializeDatabase() {
        // Database initialization handled by Room
    }
}