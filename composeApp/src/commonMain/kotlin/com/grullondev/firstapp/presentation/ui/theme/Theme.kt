package com.grullondev.firstapp.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(
    userDarkModePref: Boolean?,
    themeColor: Color,
    content: @Composable () -> Unit
) {
    val isDarkMode = userDarkModePref ?: isSystemInDarkTheme()

    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = themeColor,
            onPrimary = Color.White,
            background = Color(0xFF0F171E),
            surface = Color(0xFF1B2733),
            surfaceVariant = Color(0xFF232D36),
            onSurface = Color.White,
            onSurfaceVariant = Color.LightGray,
            tertiaryContainer = themeColor.copy(alpha = 0.3f),
            secondaryContainer = Color(0xFF232D36),
            onSecondaryContainer = Color.White
        )
    } else {
        lightColorScheme(
            primary = themeColor,
            onPrimary = Color.White,
            background = Color(0xFFE5DDD5),
            surface = Color.White,
            surfaceVariant = Color(0xFFF0F2F5),
            onSurface = Color.Black,
            onSurfaceVariant = Color.Gray,
            tertiaryContainer = themeColor.copy(alpha = 0.2f),
            secondaryContainer = Color.White,
            onSecondaryContainer = Color.Black
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
