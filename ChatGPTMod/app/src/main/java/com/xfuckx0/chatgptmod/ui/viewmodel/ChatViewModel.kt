package com.xfuckx0.chatgptmod.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.xfuckx0.chatgptmod.data.ChatRepository
import com.xfuckx0.chatgptmod.network.ApiConfig
import com.xfuckx0.chatgptmod.network.Message
import com.xfuckx0.chatgptmod.network.OpenCodeApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream

data class PendingAttachment(
    val name: String,
    val type: String,
    val content: String
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val openCodeApi: OpenCodeApi
) : ViewModel() {
    private val disposable = CompositeDisposable()
    val uiState = repository.uiState

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _attachment = MutableStateFlow<PendingAttachment?>(null)
    val attachment = _attachment.asStateFlow()

    fun setInputText(text: String) { _inputText.value = text }
    fun removeAttachment() { _attachment.value = null }

    fun attach(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val item = withContext(Dispatchers.IO) {
                    val resolver = context.contentResolver
                    val mime = resolver.getType(uri)?.lowercase() ?: ""
                    val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                        if (it.moveToFirst()) it.getString(0) else null
                    } ?: "attachment"
                    val limit = 4 * 1024 * 1024
                    val bytes = resolver.openInputStream(uri)?.use { input ->
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            if (output.size() + count > limit) error("Files must be smaller than 4 MB.")
                            output.write(buffer, 0, count)
                        }
                        output.toByteArray()
                    } ?: error("Unable to open this file.")

                    val lower = name.lowercase()
                    when {
                        mime in listOf("image/jpeg", "image/png", "image/webp") -> {
                            PendingAttachment(name, "image", "data:$mime;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP))
                        }
                        mime == "application/pdf" || lower.endsWith(".pdf") -> {
                            PDFBoxResourceLoader.init(context.applicationContext)
                            val extracted = PDDocument.load(bytes).use { PDFTextStripper().getText(it) }
                            if (extracted.isBlank()) error("This PDF has no selectable text. Scanned PDFs need OCR.")
                            PendingAttachment(name, "text", extracted.take(30000))
                        }
                        mime.startsWith("text/") || mime in listOf("application/json", "application/xml") ||
                            listOf(".txt", ".md", ".csv", ".json", ".xml", ".log", ".kt", ".java", ".py", ".js").any { lower.endsWith(it) } ->
                            PendingAttachment(name, "text", bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF").take(30000))
                        else -> error("Supported attachments: JPG, PNG, WebP, PDF with text, TXT, MD, CSV, JSON and source code.")
                    }
                }
                _attachment.value = item
                repository.clearError()
            } catch (error: Exception) {
                repository.setError(error.message ?: "Could not attach this file.")
            }
        }
    }

    fun sendMessage() {
        val rawText = _inputText.value.trim()
        val attached = _attachment.value
        if ((rawText.isBlank() && attached == null) || repository.isLoading.value) return
        if (ApiConfig.apiKey.isBlank()) {
            repository.setError("Open Settings and add your OpenCode Zen API key before sending.")
            return
        }
        val prompt = rawText.ifBlank {
            if (attached?.type == "image") "Describe this image." else "Summarize the attached file."
        }
        val visibleText = if (attached == null) prompt else "$prompt\n\n[Attached: ${attached.name}]"
        _inputText.value = ""
        _attachment.value = null
        repository.setLoading(true)
        repository.clearError()

        viewModelScope.launch {
            try {
                if (repository.currentConversationId.value == null) repository.createNewConversation()
                repository.addUserMessage(visibleText)
                val conversationId = repository.currentConversationId.value ?: error("No active conversation")
                val messages = repository.getMessagesSnapshot(conversationId)
                    .map { Message(it.role, it.content) }.toMutableList()
                if (attached != null && messages.isNotEmpty()) {
                    messages[messages.lastIndex] = if (attached.type == "image") {
                        val content = buildJsonArray {
                            add(buildJsonObject { put("type", "text"); put("text", prompt) })
                            add(buildJsonObject {
                                put("type", "image_url")
                                put("image_url", buildJsonObject { put("url", attached.content) })
                            })
                        }
                        Message("user", content)
                    } else {
                        Message("user", "$prompt\n\nAttached file: ${attached.name}\n\n${attached.content}")
                    }
                }
                disposable.add(
                    openCodeApi.sendMessage(messages)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { reply ->
                                viewModelScope.launch {
                                    repository.addAssistantMessage(reply, conversationId)
                                    repository.setLoading(false)
                                }
                            },
                            { error ->
                                repository.setLoading(false)
                                repository.setError(error.message ?: "Could not send your message.")
                            }
                        )
                )
            } catch (error: Exception) {
                repository.setLoading(false)
                repository.setError(error.message ?: "Could not send your message.")
            }
        }
    }

    fun createNewChat() { viewModelScope.launch { repository.createNewConversation() } }
    fun deleteConversation(id: Long) { viewModelScope.launch { repository.deleteConversation(id) } }
    fun selectConversation(id: Long) { repository.loadMessages(id) }
    override fun onCleared() { disposable.clear(); super.onCleared() }
}
