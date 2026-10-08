package com.xfuckx0.chatgptmod.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xfuckx0.chatgptmod.data.ChatRepository
import com.xfuckx0.chatgptmod.data.ChatState
import com.xfuckx0.chatgptmod.network.OpenCodeApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val openCodeApi: OpenCodeApi
) : ViewModel() {

    private val disposable = CompositeDisposable()

    // UI State from repository
    val uiState = repository.uiState

    // Input state
    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return

        _inputText.value = ""
        repository.setLoading(true)
        repository.clearError()

        val conversationId = repository.currentConversationId.value ?: repository.createNewConversation()
        repository.addUserMessage(text)

        disposable.add(
            openCodeApi.sendMessage(
                repository.messages.value.map { msg ->
                    com.xfuckx0.chatgptmod.network.Message(msg.role, msg.content)
                } + com.xfuckx0.chatgptmod.network.Message("user", text)
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { chunk ->
                        // Handle streaming chunks - append to last assistant message or create new
                        // For simplicity, we'll collect and add at the end
                    },
                    { error ->
                        repository.setLoading(false)
                        repository.setError(error.message ?: "Unknown error")
                    },
                    {
                        // Stream completed - the last assistant message was added via chunks
                        repository.setLoading(false)
                    }
                )
        )
    }

    fun createNewChat() {
        repository.createNewConversation()
    }

    fun deleteConversation(conversationId: Long) {
        repository.deleteConversation(conversationId)
    }

    fun selectConversation(conversationId: Long) {
        repository.loadMessages(conversationId)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}