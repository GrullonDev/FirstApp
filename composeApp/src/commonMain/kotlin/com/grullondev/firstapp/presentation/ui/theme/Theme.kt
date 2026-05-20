package com.grullondev.firstapp.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAvatarPalette = compositionLocalOf {
    listOf(
        Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFF6A1B9A),
        Color(0xFFC62828), Color(0xFF00838F), Color(0xFF4527A0),
        Color(0xFF558B2F), Color(0xFFE65100)
    )
}

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
        colorScheme = colorScheme
    ) {
        CompositionLocalProvider(
            LocalAvatarPalette provides if (isDarkMode) {
                listOf(
                    Color(0xFF90CAF9), Color(0xFFA5D6A7), Color(0xFFCE93D8),
                    Color(0xFFEF9A9A), Color(0xFF80DEEA), Color(0xFFB39DDB),
                    Color(0xFFC5E1A5), Color(0xFFFFCC80)
                )
            } else {
                listOf(
                    Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFF6A1B9A),
                    Color(0xFFC62828), Color(0xFF00838F), Color(0xFF4527A0),
                    Color(0xFF558B2F), Color(0xFFE65100)
                )
            }
        ) {
            content()
        }
    }
}

@Composable
fun avatarColorFromName(name: String): Color {
    val palette = LocalAvatarPalette.current
    return palette[kotlin.math.abs(name.hashCode()) % palette.size]
}
