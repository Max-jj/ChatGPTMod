package com.xfuckx0.chatgptmod.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface ApiService {

    @Streaming
    @Headers(
        "Content-Type: application/json",
        "Accept: text/event-stream"
    )
    @POST("chat/completions")
    fun chatCompletions(
        @Body request: ChatCompletionRequest
    ): Call<okhttp3.ResponseBody>

    @POST("chat/completions")
    fun chatCompletionsNonStream(
        @Body request: ChatCompletionRequest
    ): Call<ChatCompletionResponse>
}

@Serializable
data class ChatCompletionRequest(
    val model: String = ApiConfig.freeModels.first(),
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    val max_tokens: Int? = 4096,
    val stream: Boolean = true,
    val top_p: Float = 1.0f,
    val frequency_penalty: Float = 0.0f,
    val presence_penalty: Float = 0.0f
)

@Serializable
data class Message(
    val role: String,
    val content: JsonElement,
    val name: String? = null
) {
    constructor(role: String, text: String) : this(role, JsonPrimitive(text))
}

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String?
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

@Serializable
data class ChatCompletionChunk(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChunkChoice>
)

@Serializable
data class ChunkChoice(
    val index: Int,
    val delta: Delta,
    val finish_reason: String?
)

@Serializable
data class Delta(
    val role: String? = null,
    val content: String? = null
)
