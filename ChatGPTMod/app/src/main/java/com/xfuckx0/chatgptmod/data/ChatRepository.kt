package com.xfuckx0.chatgptmod.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatRepository(context: Context) {
    private val database = ChatDatabase.getDatabase(context)
    private val dao = database.chatDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId = _currentConversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val uiState = combine(
        conversations, currentConversationId, messages, isLoading, error
    ) { convs, currentId, msgs, loading, err ->
        ChatState(convs, currentId, msgs, loading, err)
    }.distinctUntilChanged()

    init {
        scope.launch {
            dao.getAllConversations().collect { list ->
                _conversations.value = list
                if (_currentConversationId.value == null && list.isNotEmpty()) {
                    loadMessages(list.first().id)
                }
            }
        }
    }

    private var messagesJob: Job? = null

    suspend fun getMessagesSnapshot(conversationId: Long): List<ChatMessage> =
        dao.getMessagesSnapshot(conversationId)

    fun loadMessages(conversationId: Long) {
        _currentConversationId.value = conversationId
        messagesJob?.cancel()
        _messages.value = emptyList()
        messagesJob = scope.launch {
            dao.getMessagesForConversation(conversationId).collect {
                _messages.value = it
            }
        }
    }

    suspend fun createNewConversation(): Long {
        val now = System.currentTimeMillis()
        val id = dao.insertConversation(
            Conversation(title = "New Chat", createdAt = now, updatedAt = now)
        )
        _currentConversationId.value = id
        messagesJob?.cancel()
        _messages.value = emptyList()
        loadMessages(id)
        return id
    }

    suspend fun addUserMessage(content: String): Long {
        val conversationId = _currentConversationId.value ?: createNewConversation()
        val id = dao.insertMessage(
            ChatMessage(role = "user", content = content, conversationId = conversationId)
        )
        updateConversation(conversationId)
        return id
    }

    suspend fun addAssistantMessage(content: String): Long {
        val conversationId = _currentConversationId.value ?: return -1
        val id = dao.insertMessage(
            ChatMessage(role = "assistant", content = content, conversationId = conversationId)
        )
        updateConversation(conversationId)
        return id
    }

    private suspend fun updateConversation(conversationId: Long) {
        val conversation = dao.getConversationById(conversationId) ?: return
        val count = dao.getMessageCount(conversationId)
        dao.updateConversation(
            conversation.copy(
                messageCount = count,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteConversation(conversationId: Long) {
        dao.getConversationById(conversationId)?.let { dao.deleteConversation(it) }
        dao.deleteMessagesForConversation(conversationId)
        val remaining = dao.getAllConversationsSnapshot()
        if (_currentConversationId.value == conversationId) {
            val next = remaining.firstOrNull()
            _currentConversationId.value = next?.id
            _messages.value = emptyList()
            next?.let { loadMessages(it.id) }
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setError(error: String?) {
        _error.value = error
    }

    fun clearError() {
        _error.value = null
    }
}