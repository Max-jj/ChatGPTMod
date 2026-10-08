package com.xfuckx0.chatgptmod.ui.screen

import android.app.Activity
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.xfuckx0.chatgptmod.network.ApiConfig
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xfuckx0.chatgptmod.R
import com.xfuckx0.chatgptmod.data.ChatMessage
import com.xfuckx0.chatgptmod.data.ChatState
import com.xfuckx0.chatgptmod.data.Conversation
import com.xfuckx0.chatgptmod.data.Session
import com.xfuckx0.chatgptmod.ui.theme.Theme
import com.xfuckx0.chatgptmod.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    session: Session,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(initialValue = ChatState())
    val inputText by viewModel.inputText.collectAsStateWithLifecycle(initialValue = "")
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(ApiConfig.apiKey.isBlank()) }
    val attachment by viewModel.attachment.collectAsStateWithLifecycle()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.attach(context, uri)
    }
    val speechPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognized = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!recognized.isNullOrBlank()) viewModel.setInputText(recognized)
        }
    }
    var showProfile by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(),
                drawerContainerColor = Color(0xFF171717)
            ) {
                SidebarContent(
                    state = uiState,
                    session = session,
                    onNewChat = {
                        viewModel.createNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onConversation = { id ->
                        viewModel.selectConversation(id)
                        scope.launch { drawerState.close() }
                    },
                    onDelete = viewModel::deleteConversation,
                    onOwner = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/XfuckX0"))
                        )
                    },
                    onAbout = { showAbout = true },
                    onProfile = { showProfile = true },
                    onLogout = onLogout
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "ChatGPT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (ApiConfig.apiKey.isBlank()) "Set up API key" else ApiConfig.model + " · Free",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Outlined.Tune, contentDescription = "Settings")
                        }
                        IconButton(onClick = { showProfile = true }) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (session.name.ifBlank { "G" }).take(1).uppercase(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        IconButton(onClick = viewModel::createNewChat) {
                            Icon(Icons.Outlined.Add, contentDescription = "New chat")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                Composer(
                    text = inputText,
                    onTextChange = viewModel::setInputText,
                    onSend = viewModel::sendMessage,
                    loading = uiState.isLoading,
                    attachmentName = attachment?.name,
                    onRemoveAttachment = viewModel::removeAttachment,
                    onAttach = { filePicker.launch("*/*") },
                    onVoice = {
                        try {
                            speechPicker.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"))
                        } catch (_: Exception) {
                            Toast.makeText(context, "Speech recognition is not available on this device.", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        ) { padding ->
            ChatContent(
                uiState = uiState,
                onSuggestion = { suggestion -> viewModel.setInputText(suggestion) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }

    if (showAbout) AboutDialog(onDismiss = { showAbout = false })
    if (showSettings) SettingsDialog(onDismiss = { showSettings = false })
    if (showProfile) {
        ProfileDialog(
            session = session,
            onDismiss = { showProfile = false },
            onLogout = onLogout
        )
    }
}

@Composable
private fun ChatContent(uiState: ChatState, onSuggestion: (String) -> Unit, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(uiState.messages.size, uiState.isLoading, uiState.error) {
        val count = uiState.messages.size + (if (uiState.isLoading) 1 else 0) + (if (uiState.error != null) 1 else 0)
        if (count > 0) listState.animateScrollToItem(count - 1)
    }
    if (uiState.messages.isEmpty() && !uiState.isLoading && uiState.error == null) {
        EmptyChat(onSuggestion, modifier)
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 22.dp)
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                MessageRow(message)
            }
            if (uiState.isLoading) item { TypingRow() }
            if (uiState.error != null) item { ErrorCard(uiState.error) }
        }
    }
}

@Composable
private fun EmptyChat(onSuggestion: (String) -> Unit, modifier: Modifier) {
    val suggestions = listOf(
        "Explain this code",
        "Build an Android app",
        "Plan a project",
        "Help me solve a problem"
    )

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 26.dp)
    ) {
        item {
            Spacer(Modifier.height(90.dp))
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF303030)
            ) {
                Image(
                    painter = painterResource(R.drawable.gpt_logo),
                    contentDescription = "ChatGPT",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(13.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "What can I help with?",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "Ask anything to get started",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(26.dp))
        }

        items(suggestions) { suggestion ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSuggestion(suggestion) },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(suggestion, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun MessageRow(message: ChatMessage) {
    val isUser = message.role == "user"
    val clipboard: ClipboardManager = LocalClipboardManager.current

    if (isUser) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.84f),
                shape = RoundedCornerShape(23.dp),
                color = Color(0xFF343434)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 17.dp, vertical = 13.dp),
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    color = Color.White
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.gpt_logo),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text("ChatGPT", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = message.content,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 2.dp)
            )
            IconButton(
                onClick = { clipboard.setText(AnnotatedString(message.content)) },
                modifier = Modifier.size(33.dp)
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Copy response",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun TypingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF2F2F2F)
        ) {
            Image(
                painter = painterResource(R.drawable.gpt_logo),
                contentDescription = "ChatGPT",
                modifier = Modifier.padding(6.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "Thinking…",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ErrorCard(error: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF3A2424)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFFFF8A80)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                error,
                color = Color(0xFFFFC7C2),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun Composer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    loading: Boolean,
    attachmentName: String?,
    onRemoveAttachment: () -> Unit,
    onAttach: () -> Unit,
    onVoice: () -> Unit
) {
    val canSend = (text.isNotBlank() || attachmentName != null) && !loading

    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column {
                if (attachmentName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 9.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            attachmentName,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = onRemoveAttachment, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove attachment", modifier = Modifier.size(17.dp))
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    IconButton(onClick = onAttach, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Outlined.Add, contentDescription = "Attach an image or document")
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 10.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, lineHeight = 21.sp),
                        singleLine = false,
                        maxLines = 7,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                        decorationBox = { inner ->
                            Box {
                                if (text.isEmpty()) Text("Message", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                                inner()
                            }
                        }
                    )
                    if (!canSend && text.isBlank()) {
                        IconButton(onClick = onVoice, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Outlined.Mic, contentDescription = "Dictate message")
                        }
                    } else {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (canSend) Color.White else Color(0xFF555555)
                        ) {
                            IconButton(onClick = { if (canSend) onSend() }, enabled = canSend) {
                                Icon(
                                    Icons.Outlined.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = if (canSend) Color.Black else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarContent(
    state: ChatState,
    session: Session,
    onNewChat: () -> Unit,
    onConversation: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onOwner: () -> Unit,
    onAbout: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.gpt_logo),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text("ChatGPT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onNewChat,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F2F2F)
            )
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New chat", fontWeight = FontWeight.SemiBold)
        }

        Text(
            "Recent chats",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            items(state.conversations, key = { it.id }) { conversation ->
                ConversationRow(
                    conversation = conversation,
                    selected = conversation.id == state.currentConversationId,
                    onClick = { onConversation(conversation.id) },
                    onDelete = { onDelete(conversation.id) }
                )
            }
        }

        HorizontalDivider(color = Color(0xFF2B2B2B))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onProfile),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = Color(0xFF3A3A3A)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (session.name.ifBlank { "G" }).take(1).uppercase(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.name.ifBlank { "Guest" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (session.email.isBlank()) "Guest mode" else session.email,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Icon(Icons.Outlined.Tune, contentDescription = "Profile")
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAbout) {
                Icon(Icons.Outlined.Info, contentDescription = "About")
            }
            IconButton(onClick = onOwner) {
                Image(
                    painter = painterResource(R.drawable.gpt_logo),
                    contentDescription = "Telegram",
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Owner @XfuckX0",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onLogout) {
                Icon(Icons.Outlined.Tune, contentDescription = "Sign out")
            }
        }

        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                if (selected) Color(0xFF2F2F2F) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            conversation.title,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
            Icon(
                Icons.Outlined.DeleteOutline,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF202020),
        icon = {
            Image(
                painter = painterResource(R.drawable.gpt_logo),
                contentDescription = null,
                modifier = Modifier.size(54.dp)
            )
        },
        title = { Text("ChatGPT Mod") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Dark interface, local chat history and guest access.")
                Text("Models: OpenCode Zen free")
                Text("Owner: @XfuckX0")
                Text("Support and updates on Telegram.")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/XfuckX0"))
                    )
                }
            ) {
                Text("Telegram")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ProfileDialog(
    session: Session,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF202020),
        title = { Text("Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Name: " + session.name.ifBlank { "Guest" })
                Text(
                    if (session.email.isBlank()) {
                        "Mode: guest"
                    } else {
                        "Email: " + session.email
                    }
                )
                Text("App author: @XfuckX0", color = MaterialTheme.colorScheme.primary)
                Text(
                    "Authentication is local to your device.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
            ) {
                Text("Sign out")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    var key by remember { mutableStateOf(ApiConfig.apiKey) }
    var model by remember { mutableStateOf(ApiConfig.model) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF242424),
        title = { Text("AI settings") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Connect your own OpenCode Zen account to chat. Free models require an API key and model access.", fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("OpenCode Zen API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Free model", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                ApiConfig.freeModels.forEach { candidate ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { model = candidate }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = model == candidate, onClick = { model = candidate })
                        Text(candidate, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Keys are stored locally on this device. This is an unofficial app; it is not connected to your ChatGPT account.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = {
                ApiConfig.apiKey = key
                ApiConfig.model = model
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
