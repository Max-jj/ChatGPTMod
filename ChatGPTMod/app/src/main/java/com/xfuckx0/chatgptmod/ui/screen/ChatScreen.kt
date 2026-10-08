package com.xfuckx0.chatgptmod.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.xfuckx0.chatgptmod.R
import com.xfuckx0.chatgptmod.ui.theme.Theme
import com.xfuckx0.chatgptmod.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showSidebar by remember { mutableStateOf(false) }
    val showCreatorPopup by remember { mutableStateOf(true) } // Show on first launch

    // Show creator popup on first launch
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (showCreatorPopup) {
            // Popup will be shown via state
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top App Bar
            TopAppBar(
                title = { Text("ChatGPT Mod", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Theme.colorScheme.surfaceContainer,
                    titleContentColor = Theme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { showSidebar = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Menu",
                            tint = Theme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.createNewChat() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "New Chat",
                            tint = Theme.colorScheme.onSurface
                        )
                    }
                }
            )

            // Messages area
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(uiState.messages.reversed()) { message ->
                    MessageBubble(message = message)
                }
                if (uiState.isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Theme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Input area
            MessageInput(viewModel = viewModel)
        }

        // Sidebar
        if (showSidebar) {
            Sidebar(
                conversations = uiState.conversations,
                currentConversationId = uiState.currentConversationId,
                onConversationClick = { id ->
                    viewModel.selectConversation(id)
                    showSidebar = false
                },
                onNewChatClick = {
                    viewModel.createNewChat()
                    showSidebar = false
                },
                onDeleteClick = { id ->
                    viewModel.deleteConversation(id)
                },
                onDismiss = { showSidebar = false }
            )
        }

        // Creator Popup
        if (showCreatorPopup) {
            CreatorPopup(onDismiss = { showCreatorPopup = false })
        }
    }
}

@Composable
fun MessageBubble(message: com.xfuckx0.chatgptmod.data.ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 8.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (isUser) Theme.colorScheme.primary else Theme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                color = if (isUser) Theme.colorScheme.onPrimary else Theme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MessageInput(viewModel: ChatViewModel) {
    var text by remember { mutableStateOf("") }
    val uiState = viewModel.uiState.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Theme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.TextField(
            value = text,
            onValueChange = { text = it; viewModel.setInputText(it) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(end = 12.dp),
            placeholder = { Text("Message...", color = Theme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            singleLine = true,
            colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                containerColor = Theme.colorScheme.surface,
                focusedContainerColor = Theme.colorScheme.surface,
                unfocusedContainerColor = Theme.colorScheme.surface
            )
        )
        Button(
            onClick = { viewModel.sendMessage() },
            enabled = text.isNotBlank() && !uiState.isLoading,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Theme.colorScheme.primary,
                disabledContainerColor = Theme.colorScheme.primary.copy(alpha = 0.4f)
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_send),
                contentDescription = "Send",
                tint = Theme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun Sidebar(
    conversations: List<com.xfuckx0.chatgptmod.data.Conversation>,
    currentConversationId: Long?,
    onConversationClick: (Long) -> Unit,
    onNewChatClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chats", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Theme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(id = R.drawable.ic_close), contentDescription = "Close", tint = Theme.colorScheme.onSurface)
                    }
                }

                androidx.compose.material3.Button(
                    onClick = onNewChatClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Theme.colorScheme.primaryContainer
                    )
                ) {
                    Text("+ New Chat", color = Theme.colorScheme.onPrimaryContainer)
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations) { conversation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .background(
                                    color = if (conversation.id == currentConversationId) Theme.colorScheme.primaryContainer else Theme.colorScheme.surface,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .clickable { onConversationClick(conversation.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = conversation.title,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                                color = if (conversation.id == currentConversationId) Theme.colorScheme.onPrimaryContainer else Theme.colorScheme.onSurface,
                                fontWeight = if (conversation.id == currentConversationId) FontWeight.Bold else FontWeight.Normal
                            )
                            IconButton(onClick = { onDeleteClick(conversation.id) }) {
                                Icon(painterResource(id = R.drawable.ic_delete), contentDescription = "Delete", tint = Theme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorPopup(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .padding(24.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo/Icon
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "ChatGPT Mod",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    "ChatGPT Mod",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Theme.colorScheme.onSurface
                )

                Text(
                    "Created by @XfuckX0",
                    fontSize = 16.sp,
                    color = Theme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    "Powered by Nemotron-3-Ultra-Free\nvia OpenCode AI",
                    fontSize = 14.sp,
                    color = Theme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )

                androidx.compose.material3.Divider(modifier = Modifier.fillMaxWidth())

                Text(
                    "No subscriptions • No limits • Fully free",
                    fontSize = 13.sp,
                    color = Theme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )

                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Theme.colorScheme.primary
                    )
                ) {
                    Text("Get Started", color = Theme.colorScheme.onPrimary)
                }
            }
        }
    }
}