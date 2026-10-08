package com.xfuckx0.chatgptmod.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialThemeButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xfuckx0.chatgptmod.R
import com.xfuckx0.chatgptmod.data.AuthResult
import com.xfuckx0.chatgptmod.data.SessionStore
import com.xfuckx0.chatgptmod.ui.theme.Theme
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(sessionStore: SessionStore) {
    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun submit() {
        error = null
        loading = true
        scope.launch {
            val result = if (isSignUp) {
                sessionStore.signUp(name, email, password)
            } else {
                sessionStore.login(email, password)
            }
            loading = false
            if (result is AuthResult.Error) {
                error = result.message
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Mod owner  @XfuckX0",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        "Supporto e aggiornamenti su Telegram",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/XfuckX0"))
                                )
                            }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = RoundedCornerShape(23.dp),
                        color = Color(0xFF2F2F2F)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.gpt_logo),
                            contentDescription = "ChatGPT Mod",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        if (isSignUp) "Crea il tuo account" else "Bentornato",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        if (isSignUp) {
                            "Personalizza la tua esperienza e mantieni le chat sul dispositivo."
                        } else {
                            "Accedi alla tua esperienza ChatGPT Mod."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFF171717)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFF222222),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(4.dp)
                            ) {
                                AuthModeButton(
                                    text = "Accedi",
                                    selected = !isSignUp,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        isSignUp = false
                                        error = null
                                    }
                                )
                                AuthModeButton(
                                    text = "Registrati",
                                    selected = isSignUp,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        isSignUp = true
                                        error = null
                                    }
                                )
                            }

                            if (isSignUp) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it; error = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Nome") },
                                    leadingIcon = { Icon(Icons.Outlined.Person, null) },
                                    shape = RoundedCornerShape(15.dp)
                                )
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; error = null },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Email") },
                                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(15.dp)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; error = null },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Outlined.VisibilityOff
                                            else Icons.Outlined.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(15.dp)
                            )

                            if (error != null) {
                                Text(
                                    text = error.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }

                            Button(
                                onClick = ::submit,
                                enabled = !loading && email.isNotBlank() && password.isNotBlank() &&
                                    (!isSignUp || name.isNotBlank()),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(53.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    if (loading) "Accessoâ€¦" else if (isSignUp) "Crea account" else "Accedi",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (!loading) {
                                        scope.launch { sessionStore.continueAsGuest() }
                                    }
                                },
                                enabled = !loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Continua come ospite")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Accesso locale â€¢ credenziali salvate sul dispositivo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthModeButton(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(1.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = if (selected) Color(0xFF3A3A3A) else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}


