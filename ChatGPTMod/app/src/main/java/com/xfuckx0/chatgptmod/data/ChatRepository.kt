package com.xfuckx0.chatgptmod.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxDataStore
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

class ChatRepository(private val context: Context) {

    private val database = ChatDatabase.getDatabase(context)
    private val dao = database.chatDao()

    // Current state
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

    // Combined state for UI
    val uiState = combine(conversations, currentConversationId, messages, isLoading, error) { convs, currentId, msgs, loading, err ->
        ChatState(convs, currentId, msgs, loading, err)
    }.distinctUntilChanged()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAllConversations().collect { list ->
                _conversations.value = list
                if (_currentConversationId.value == null && list.isNotEmpty()) {
                    _currentConversationId.value = list.first().id
                    loadMessages(list.first().id)
                }
            }
        }
    }

    fun loadMessages(conversationId: Long) {
        _currentConversationId.value = conversationId
        CoroutineScope(Dispatchers.IO).launch {
            dao.getMessagesForConversation(conversationId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun createNewConversation(): Long {
        val conversation = Conversation(
            title = "New Chat",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return CoroutineScope(Dispatchers.IO).launch {
            val id = dao.insertConversation(conversation)
            _currentConversationId.value = id
            loadMessages(id)
            id
        }.join()
    }

    fun updateConversationTitle(conversationId: Long, title: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val conversation = dao.getConversationById(conversationId)?.copy(
                title = title,
                updatedAt = System.currentTimeMillis()
            )
            conversation?.let { dao.updateConversation(it) }
        }
    }

    fun deleteConversation(conversationId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val conversation = dao.getConversationById(conversationId)
            conversation?.let { dao.deleteConversation(it) }
            dao.deleteMessagesForConversation(conversationId)
            if (_currentConversationId.value == conversationId) {
                val remaining = _conversations.value
                _currentConversationId.value = remaining.firstOrNull()?.id
                _currentConversationId.value?.let { loadMessages(it) }
            }
        }
    }

    fun addUserMessage(content: String): Long {
        val conversationId = _currentConversationId.value ?: createNewConversation()
        val message = ChatMessage(
            role = "user",
            content = content,
            conversationId = conversationId
        )
        return CoroutineScope(Dispatchers.IO).launch {
            val id = dao.insertMessage(message)
            updateConversationTimestamp(conversationId)
            id
        }.join()
    }

    fun addAssistantMessage(content: String): Long {
        val conversationId = _currentConversationId.value ?: return -1
        val message = ChatMessage(
            role = "assistant",
            content = content,
            conversationId = conversationId
        )
        return CoroutineScope(Dispatchers.IO).launch {
            val id = dao.insertMessage(message)
            updateConversationTimestamp(conversationId)
            updateConversationMessageCount(conversationId)
            id
        }.join()
    }

    private fun updateConversationTimestamp(conversationId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val conversation = dao.getConversationById(conversationId)?.copy(
                updatedAt = System.currentTimeMillis()
            )
            conversation?.let { dao.updateConversation(it) }
        }
    }

    private fun updateConversationMessageCount(conversationId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val count = dao.getMessageCount(conversationId)
            val conversation = dao.getConversationById(conversationId)?.copy(
                messageCount = count,
                updatedAt = System.currentTimeMillis()
            )
            conversation?.let { dao.updateConversation(it) }
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