package com.xfuckx0.chatgptmod.data

import android.content.Context
import android.util.Patterns
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.sessionDataStore by preferencesDataStore(name = "chatgpt_mod_session")

enum class SessionMode {
    NONE, USER, GUEST
}

data class Session(
    val mode: SessionMode = SessionMode.NONE,
    val name: String = "",
    val email: String = ""
)

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class SessionStore(private val context: Context) {

    private val modeKey = stringPreferencesKey("mode")
    private val nameKey = stringPreferencesKey("name")
    private val emailKey = stringPreferencesKey("email")
    private val accountEmailKey = stringPreferencesKey("account_email")
    private val accountNameKey = stringPreferencesKey("account_name")
    private val accountPasswordHashKey = stringPreferencesKey("account_password_hash")

    val session: Flow<Session> = context.sessionDataStore.data.map { prefs ->
        Session(
            mode = when (prefs[modeKey]) {
                "user" -> SessionMode.USER
                "guest" -> SessionMode.GUEST
                else -> SessionMode.NONE
            },
            name = prefs[nameKey].orEmpty(),
            email = prefs[emailKey].orEmpty()
        )
    }

    suspend fun signUp(name: String, email: String, password: String): AuthResult {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.length < 2) return AuthResult.Error("Inserisci un nome valido.")
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return AuthResult.Error("Inserisci un indirizzo email valido.")
        }
        if (password.length < 6) {
            return AuthResult.Error("La password deve avere almeno 6 caratteri.")
        }

        context.sessionDataStore.edit { prefs ->
            prefs[accountEmailKey] = cleanEmail
            prefs[accountNameKey] = cleanName
            prefs[accountPasswordHashKey] = hash(password)
            prefs[modeKey] = "user"
            prefs[nameKey] = cleanName
            prefs[emailKey] = cleanEmail
        }

        return AuthResult.Success
    }

    suspend fun login(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return AuthResult.Error("Email non valida.")
        }

        var valid = false
        var storedName = ""

        context.sessionDataStore.edit { prefs ->
            val storedEmail = prefs[accountEmailKey]
            val storedHash = prefs[accountPasswordHashKey]
            valid = storedEmail == cleanEmail && storedHash == hash(password)
            storedName = prefs[accountNameKey].orEmpty()

            if (valid) {
                prefs[modeKey] = "user"
                prefs[nameKey] = storedName.ifBlank { cleanEmail.substringBefore("@") }
                prefs[emailKey] = cleanEmail
            }
        }

        return if (valid) AuthResult.Success
        else AuthResult.Error("Email o password non corrette.")
    }

    suspend fun continueAsGuest() {
        context.sessionDataStore.edit { prefs ->
            prefs[modeKey] = "guest"
            prefs[nameKey] = "Guest"
            prefs[emailKey] = ""
        }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(modeKey)
            prefs.remove(nameKey)
            prefs.remove(emailKey)
        }
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
