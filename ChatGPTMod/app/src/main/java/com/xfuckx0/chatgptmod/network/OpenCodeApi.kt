package com.xfuckx0.chatgptmod.network

import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

class OpenCodeApi(private val apiService: ApiService) {

    private val authToken = "oc_sk_cbfb16b00582_gNYTfgWtx-v-5ChvvTcB7JFPi8jTR6HX"
    private val model = "nemotron-3-ultra-free"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer $authToken")
            .header("Accept", "text/event-stream")
            .method(original.method, original.body)
            .build()
        chain.proceed(authenticated)
    }

    fun sendMessage(messages: List<Message>): Flowable<String> {
        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            stream = true
        )

        return Flowable.create({ emitter ->
            apiService.chatCompletions(request).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        emitter.onError(IOException("API Error: ${response.code()} - $errorBody"))
                        return
                    }

                    val body = response.body() ?: return
                    val source = body.source()

                    try {
                        while (!source.exhausted()) {
                            val line = source.readUtf8LineStrict() ?: continue
                            if (line.startsWith("data: ")) {
                                val data = line.substring(6).trim()
                                if (data == "[DONE]") {
                                    emitter.onComplete()
                                    return
                                }
                                try {
                                    val chunk = Json { ignoreUnknownKeys = true }.decodeFromString<ChatCompletionChunk>(data)
                                    val content = chunk.choices.firstOrNull()?.delta?.content
                                    if (content != null && content.isNotBlank()) {
                                        emitter.onNext(content)
                                    }
                                    if (chunk.choices.firstOrNull()?.finish_reason != null) {
                                        emitter.onComplete()
                                        return
                                    }
                                } catch (e: Exception) {
                                    // Skip malformed chunks
                                }
                            }
                        }
                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    } finally {
                        body.close()
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    emitter.onError(t)
                }
            })
        }, BackpressureStrategy.BUFFER)
    }

    fun sendMessageNonStream(messages: List<Message>): Single<String> {
        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            stream = false
        )

        return Single.create { emitter ->
            apiService.chatCompletionsNonStream(request).enqueue(object : retrofit2.Callback<ChatCompletionResponse> {
                override fun onResponse(call: retrofit2.Call<ChatCompletionResponse>, response: retrofit2.Response<ChatCompletionResponse>) {
                    if (response.isSuccessful) {
                        val content = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                        emitter.onSuccess(content)
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        emitter.onError(IOException("API Error: ${response.code()} - $errorBody"))
                    }
                }

                override fun onFailure(call: retrofit2.Call<ChatCompletionResponse>, t: Throwable) {
                    emitter.onError(t)
                }
            })
        }
    }

    companion object {
        fun createClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val original = chain.request()
                    val authenticated = original.newBuilder()
                        .header("Authorization", "Bearer oc_sk_cbfb16b00582_gNYTfgWtx-v-5ChvvTcB7JFPi8jTR6HX")
                        .header("Accept", "text/event-stream")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(authenticated)
                })
                .build()
        }
    }
}