package com.xfuckx0.chatgptmod.ui.screen

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
                                "Nemotron 3 Ultra â€¢ Free",
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
                            Icon(Icons.Outlined.Add, contentDescription = "Nuova chat")
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
                    loading = uiState.isLoading
                )
            }
        ) { padding ->
            ChatContent(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }

    if (showAbout) AboutDialog(onDismiss = { showAbout = false })
    if (showProfile) {
        ProfileDialog(
            session = session,
            onDismiss = { showProfile = false },
            onLogout = onLogout
        )
    }
}

@Composable
private fun ChatContent(uiState: ChatState, modifier: Modifier = Modifier) {
    if (uiState.messages.isEmpty() && !uiState.isLoading && uiState.error == null) {
        EmptyChat(modifier)
    } else {
        LazyColumn(
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
private fun EmptyChat(modifier: Modifier) {
    val suggestions = listOf(
        "Spiegami questo codice",
        "Scrivimi un'app Android",
        "Crea un piano per un progetto",
        "Analizza questo problema"
    )

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 26.dp)
    ) {
        item {
            Spacer(Modifier.height(28.dp))
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF2F2F2F)
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
                "Come posso aiutarti?",
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "Chiedimi qualsiasi cosa.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(26.dp))
        }

        items(suggestions) { suggestion ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = if (isUser) CircleShape else RoundedCornerShape(10.dp),
            color = if (isUser) Color(0xFF5E5E5E) else Color(0xFF2F2F2F)
        ) {
            if (isUser) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Tu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.gpt_logo),
                    contentDescription = "Assistant",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isUser) "Tu" else "ChatGPT",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                message.content,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            if (!isUser) {
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(
                        onClick = { clipboard.setText(AnnotatedString(message.content)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Copia",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.MoreHoriz,
                            contentDescription = "Altre opzioni",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
            "Sta scrivendoâ€¦",
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
    loading: Boolean
) {
    val canSend = text.isNotBlank() && !loading

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Add, contentDescription = "Allega")
                }

                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    ),
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (canSend) onSend()
                    }),
                    decorationBox = { innerTextField ->
                        if (text.isBlank()) {
                            Text(
                                "Messaggioâ€¦",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (text.isBlank()) {
                    IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Outlined.Mic, contentDescription = "Voce")
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (canSend) Color(0xFFFFFFFF) else Color(0xFF5A5A5A)
                    ) {
                        IconButton(onClick = { if (canSend) onSend() }) {
                            Icon(
                                Icons.Outlined.ArrowUpward,
                                contentDescription = "Invia",
                                tint = if (canSend) Color.Black else Color(0xFF9A9A9A)
                            )
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
            Text("ChatGPT Mod", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            Text("Nuova chat", fontWeight = FontWeight.SemiBold)
        }

        Text(
            "Cronologia",
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
                        if (session.email.isBlank()) "Accesso ospite" else session.email,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Icon(Icons.Outlined.Tune, contentDescription = "Profilo")
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAbout) {
                Icon(Icons.Outlined.Info, contentDescription = "Info")
            }
            IconButton(onClick = onOwner) {
                Image(
                    painter = painterResource(R.drawable.gpt_logo),
                    contentDescription = "Owner Telegram",
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
                Icon(Icons.Outlined.Tune, contentDescription = "Esci")
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
                contentDescription = "Elimina",
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
                Text("UI dark avanzata, cronologia locale e accesso ospite.")
                Text("Modello: Nemotron 3 Ultra Free")
                Text("Owner: @XfuckX0")
                Text("Supporto e aggiornamenti su Telegram.")
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
                Text("Chiudi")
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
        title = { Text("Profilo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Nome: " + session.name.ifBlank { "Guest" })
                Text(
                    if (session.email.isBlank()) {
                        "ModalitÃ : ospite"
                    } else {
                        "Email: " + session.email
                    }
                )
                Text("Owner della mod: @XfuckX0", color = MaterialTheme.colorScheme.primary)
                Text(
                    "L'autenticazione Ã¨ locale sul dispositivo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
            ) {
                Text("Esci")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

