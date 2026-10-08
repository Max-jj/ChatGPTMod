package com.xfuckx0.chatgptmod.network

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenCodeApi(private val apiService: ApiService) {

    // Non-streaming responses avoid silent failures caused by partial/malformed SSE events.
    fun sendMessage(messages: List<Message>): Flowable<String> =
        sendMessageNonStream(messages).toFlowable()

    fun sendMessageNonStream(messages: List<Message>): Single<String> =
        Single.create { emitter ->
            if (ApiConfig.apiKey.isBlank()) {
                emitter.onError(IOException("Open Settings and add your own OpenCode Zen API key to chat."))
                return@create
            }
            val candidates = (listOf(ApiConfig.model) + ApiConfig.freeModels).distinct()
            fun attempt(index: Int) {
                if (emitter.isDisposed) return
                val model = candidates[index]
                val request = ChatCompletionRequest(
                    model = model,
                    messages = messages,
                    stream = false
                )
                apiService.chatCompletionsNonStream(request).enqueue(object : Callback<ChatCompletionResponse> {
                    override fun onResponse(call: Call<ChatCompletionResponse>, response: Response<ChatCompletionResponse>) {
                        if (emitter.isDisposed) return
                        if (response.isSuccessful) {
                            val reply = (response.body()?.choices?.firstOrNull()?.message?.content as? JsonPrimitive)
                                ?.content.orEmpty()
                            if (reply.isNotBlank()) {
                                ApiConfig.model = model
                                emitter.onSuccess(reply)
                            } else {
                                emitter.onError(IOException("The provider returned an empty response. Select another model in Settings."))
                            }
                            return
                        }
                        val details = response.errorBody()?.string().orEmpty().take(1200)
                        val lower = details.lowercase()
                        val unsupported = lower.contains("model is not supported") ||
                            lower.contains("model_not_supported") ||
                            lower.contains("unsupported model") ||
                            lower.contains("model not found") ||
                            lower.contains("model_not_found")
                        if (unsupported && index + 1 < candidates.size) {
                            attempt(index + 1)
                            return
                        }
                        val message = when {
                            response.code() == 401 -> "Invalid OpenCode Zen API key. Update it in Settings."
                            response.code() == 403 -> "This API key is not allowed to use the selected model. Check model access in your Zen account."
                            unsupported -> "No free model is enabled for this API key. Check your Zen account and Settings."
                            lower.contains("image") && (lower.contains("support") || lower.contains("modality")) ->
                                "This model cannot read images. Try a vision-enabled model."
                            response.code() == 429 -> "Provider rate limit reached. Try again later."
                            else -> "Provider error ${response.code()}: ${details.take(200).ifBlank { "Please check your connection and API account." }}"
                        }
                        emitter.onError(IOException(message))
                    }

                    override fun onFailure(call: Call<ChatCompletionResponse>, error: Throwable) {
                        if (!emitter.isDisposed) {
                            emitter.onError(IOException(error.message ?: "Network request failed"))
                        }
                    }
                })
            }
            attempt(0)
        }

    companion object {
        fun createClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val key = ApiConfig.apiKey
                if (key.isBlank()) throw IOException("Set your OpenCode Zen API key in Settings.")
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $key")
                        .header("Accept", "application/json")
                        .build()
                )
            })
            .callTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }
}
