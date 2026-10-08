package com.xfuckx0.chatgptmod.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4AA),
    primaryContainer = Color(0xFF004D3A),
    secondary = Color(0xFF6C757D),
    secondaryContainer = Color(0xFF1F2937),
    tertiary = Color(0xFF8B5CF6),
    tertiaryContainer = Color(0xFF2E1065),
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF1F2937),
    background = Color(0xFF0F172A),
    error = Color(0xFFF87171),
    onPrimary = Color(0xFF001F1A),
    onPrimaryContainer = Color(0xFF00D4AA),
    onSecondary = Color.White,
    onSecondaryContainer = Color(0xFF6C757D),
    onTertiary = Color.White,
    onTertiaryContainer = Color(0xFF8B5CF6),
    onSurface = Color(0xFFF9FAFB),
    onSurfaceVariant = Color(0xFF9CA3AF),
    onBackground = Color(0xFFF9FAFB),
    onError = Color(0xFF7F1D1D),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF1F2937),
    shadow = Color.Black,
    scrim = Color.Black,
    inverseSurface = Color(0xFFF9FAFB),
    inverseOnSurface = Color(0xFF111827),
    inversePrimary = Color(0xFF004D3A)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008A6B),
    primaryContainer = Color(0xFF00D4AA),
    secondary = Color(0xFF475569),
    secondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF7C3AED),
    tertiaryContainer = Color(0xFFEDE9FE),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    background = Color(0xFFFAFAFA),
    error = Color(0xFFDC2626),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFF002B1F),
    onSecondary = Color.White,
    onSecondaryContainer = Color(0xFF1E293B),
    onTertiary = Color.White,
    onTertiaryContainer = Color(0xFF312E81),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    onBackground = Color(0xFF0F172A),
    onError = Color.White,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    shadow = Color.Black,
    scrim = Color.Black,
    inverseSurface = Color(0xFF111827),
    inverseOnSurface = Color(0xFFF9FAFB),
    inversePrimary = Color(0xFF00D4AA)
)

@Composable
fun Theme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}