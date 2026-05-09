package com.grullondev.firstapp

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import com.grullondev.firstapp.data.repository.InMemoryChatRepository
import com.grullondev.firstapp.data.repository.InMemorySettingsRepository
import com.grullondev.firstapp.data.repository.MockPermissionManager
import com.grullondev.firstapp.data.repository.PersistentSettingsRepository
import com.grullondev.firstapp.presentation.ui.CommonBackHandler
import com.grullondev.firstapp.presentation.ui.ChatListScreen
import com.grullondev.firstapp.presentation.ui.ChatScreen
import com.grullondev.firstapp.presentation.ui.ContactProfileScreen
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel

@Composable
@Preview
fun App() {
    // En una aplicación real, esto se manejaría con Inyección de Dependencias (ej. Koin)
    val isPreview = LocalInspectionMode.current
    val repository = remember { InMemoryChatRepository() }
    val permissionManager = remember { MockPermissionManager() }
    val settingsRepository = remember { 
        if (isPreview) InMemorySettingsRepository() else PersistentSettingsRepository() 
    }
    val viewModel = remember { ChatViewModel(repository, permissionManager, settingsRepository) }
    
    val selectedChatId by viewModel.selectedChatId.collectAsState()
    val showContactProfile by viewModel.showContactProfile.collectAsState()
    val userDarkModePref by viewModel.isDarkMode.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val chats by viewModel.chats.collectAsState()
    
    // Si la preferencia es null (por defecto), seguimos al sistema
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

    MaterialTheme(colorScheme = colorScheme) {
        CommonBackHandler(enabled = selectedChatId != null || showContactProfile != null) {
            if (showContactProfile != null) viewModel.showProfile(null)
            else viewModel.onBackPress()
        }
        Surface(color = MaterialTheme.colorScheme.background) {
            Box {
                AnimatedContent(
                    targetState = selectedChatId,
                    transitionSpec = {
                        if (targetState != null) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it / 2 } + fadeOut()
                        } else {
                            slideInHorizontally { -it / 2 } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    }
                ) { chatId ->
                    if (chatId == null) {
                        ChatListScreen(viewModel = viewModel)
                    } else {
                        ChatScreen(viewModel = viewModel)
                    }
                }

                // Superposición del perfil de contacto
                AnimatedVisibility(
                    visible = showContactProfile != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    val profileId = showContactProfile
                    val chat = chats.find { it.id == profileId }
                    if (chat != null) {
                        ContactProfileScreen(
                            chat = chat,
                            themeColor = themeColor,
                            onBack = { viewModel.showProfile(null) }
                        )
                    }
                }
            }
        }
    }
}