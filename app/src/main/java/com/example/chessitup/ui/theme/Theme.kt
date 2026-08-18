package com.example.chessitup.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81B64C),     // Chess green accent
    secondary = Color(0xFFB58863),   // Wooden board square
    tertiary = Color(0xFFF0D9B5),    // Light board square
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF262421)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF629924),
    secondary = Color(0xFF8B6440),
    tertiary = Color(0xFFE8D0AA),
    background = Color(0xFFF7F7F7),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun ChessItUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
