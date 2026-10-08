package com.xfuckx0.chatgptmod.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xfuckx0.chatgptmod.ChatGPTModApplication
import com.xfuckx0.chatgptmod.ui.screen.ChatScreen
import com.xfuckx0.chatgptmod.ui.theme.Theme
import com.xfuckx0.chatgptmod.ui.viewmodel.ChatViewModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                ChatScreen(viewModel = viewModel)
            }
        }
    }
}