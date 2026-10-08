package com.xfuckx0.chatgptmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xfuckx0.chatgptmod.data.ChatRepository
import com.xfuckx0.chatgptmod.network.Message
import com.xfuckx0.chatgptmod.network.OpenCodeApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val openCodeApi: OpenCodeApi
) : ViewModel() {

    private val disposable = CompositeDisposable()

    val uiState = repository.uiState

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank() || repository.isLoading.value) return

        _inputText.value = ""
        repository.setLoading(true)
        repository.clearError()

        viewModelScope.launch {
            try {
                if (repository.currentConversationId.value == null) {
                    repository.createNewConversation()
                }
                repository.addUserMessage(text)

                val conversationId = repository.currentConversationId.value ?: error("Conversazione non disponibile")
                val messages = repository.getMessagesSnapshot(conversationId).map {
                    Message(it.role, it.content)
                }

                val response = StringBuilder()
                disposable.add(
                    openCodeApi.sendMessage(messages)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { chunk -> response.append(chunk) },
                            { error ->
                                repository.setLoading(false)
                                repository.setError(error.message ?: "Unknown error")
                            },
                            {
                                if (response.isNotEmpty()) {
                                    viewModelScope.launch {
                                        repository.addAssistantMessage(response.toString())
                                        repository.setLoading(false)
                                    }
                                } else {
                                    repository.setLoading(false)
                                }
                            }
                        )
                )
            } catch (error: Exception) {
                repository.setLoading(false)
                repository.setError(error.message ?: "Unable to send message")
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch { repository.createNewConversation() }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch { repository.deleteConversation(conversationId) }
    }

    fun selectConversation(conversationId: Long) {
        repository.loadMessages(conversationId)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}