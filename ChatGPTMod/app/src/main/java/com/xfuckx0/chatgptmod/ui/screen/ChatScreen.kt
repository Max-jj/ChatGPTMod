package com.xfuckx0.chatgptmod.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xfuckx0.chatgptmod.R
import com.xfuckx0.chatgptmod.data.ChatMessage
import com.xfuckx0.chatgptmod.data.Conversation
import com.xfuckx0.chatgptmod.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showSidebar by remember { mutableStateOf(false) }
    var showCreatorPopup by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("ChatGPT Mod", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton({ showSidebar = true }) {
                        Icon(painterResource(R.drawable.ic_menu), "Menu")
                    }
                },
                actions = {
                    IconButton({ viewModel.createNewChat() }) {
                        Icon(painterResource(R.drawable.ic_add), "New chat")
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                reverseLayout = false,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages) { message -> MessageBubble(message) }
                if (uiState.isLoading) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        }
                    }
                }
                uiState.error?.let { error ->
                    item {
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            MessageInput(viewModel)
        }

        if (showSidebar) {
            Sidebar(
                conversations = uiState.conversations,
                currentConversationId = uiState.currentConversationId,
                onConversationClick = { viewModel.selectConversation(it); showSidebar = false },
                onNewChatClick = { viewModel.createNewChat(); showSidebar = false },
                onDeleteClick = viewModel::deleteConversation,
                onDismiss = { showSidebar = false }
            )
        }

        if (showCreatorPopup) {
            CreatorPopup { showCreatorPopup = false }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                message.content,
                Modifier.padding(16.dp),
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MessageInput(viewModel: ChatViewModel) {
    var text by remember { mutableStateOf("") }

    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                viewModel.setInputText(it)
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message...") },
            singleLine = true
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                viewModel.sendMessage()
                text = ""
            },
            enabled = text.isNotBlank()
        ) {
            Icon(painterResource(R.drawable.ic_send), "Send")
        }
    }
}

@Composable
private fun Sidebar(
    conversations: List<Conversation>,
    currentConversationId: Long?,
    onConversationClick: (Long) -> Unit,
    onNewChatClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)).clickable { onDismiss() }
    ) {
        Card(
            Modifier.width(300.dp).fillMaxHeight().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chats", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onDismiss) {
                        Icon(painterResource(R.drawable.ic_close), "Close")
                    }
                }

                Button(onClick = onNewChatClick, Modifier.fillMaxWidth()) {
                    Text("New Chat")
                }

                LazyColumn(
                    Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations) { conversation ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                onConversationClick(conversation.id)
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                conversation.title,
                                Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton({ onDeleteClick(conversation.id) }) {
                                Icon(painterResource(R.drawable.ic_delete), "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorPopup(onDismiss: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Card(Modifier.width(320.dp).padding(24.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
                Text("ChatGPT Mod", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Created by @XfuckX0", color = MaterialTheme.colorScheme.primary)
                Text(
                    "Powered by Nemotron-3-Ultra-Free via OpenCode AI",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Button(onClick = onDismiss, Modifier.fillMaxWidth()) {
                    Text("Get Started")
                }
            }
        }
    }
}