package com.xfuckx0.chatgptmod.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.xfuckx0.chatgptmod.ChatGPTModApplication
import com.xfuckx0.chatgptmod.data.Session
import com.xfuckx0.chatgptmod.data.SessionMode
import com.xfuckx0.chatgptmod.ui.screen.AuthScreen
import com.xfuckx0.chatgptmod.ui.screen.ChatScreen
import com.xfuckx0.chatgptmod.ui.theme.Theme
import com.xfuckx0.chatgptmod.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as ChatGPTModApplication
                return ChatViewModel(app.chatRepository, app.openCodeApi) as T
            }
        }
    }

    private val sessionStore by lazy {
        (application as ChatGPTModApplication).sessionStore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Theme {
                val session = sessionStore.session.collectAsState(initial = Session()).value

                when (session.mode) {
                    SessionMode.NONE -> AuthScreen(sessionStore)
                    SessionMode.USER,
                    SessionMode.GUEST -> {
                        ChatScreen(
                            viewModel = viewModel,
                            session = session,
                            onLogout = {
                                lifecycleScope.launch { sessionStore.logout() }
                            }
                        )
                    }
                }
            }
        }
    }
}


