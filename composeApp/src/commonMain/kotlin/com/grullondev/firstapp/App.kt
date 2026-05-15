package com.grullondev.firstapp

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.grullondev.firstapp.presentation.ui.*
import com.grullondev.firstapp.presentation.ui.theme.AppTheme
import com.grullondev.firstapp.presentation.viewmodel.CalendarViewModel
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import com.grullondev.firstapp.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    // ViewModels inyectados mediante Koin
    val chatViewModel: ChatViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val calendarViewModel: CalendarViewModel = koinViewModel()
    
    val selectedChatId by chatViewModel.selectedChatId.collectAsState()
    val showContactProfile by chatViewModel.showContactProfile.collectAsState()
    val userDarkModePref by settingsViewModel.isDarkMode.collectAsState()
    val themeColor by settingsViewModel.themeColor.collectAsState()
    val chats by chatViewModel.chats.collectAsState()

    AppTheme(
        userDarkModePref = userDarkModePref,
        themeColor = themeColor
    ) {
        CommonBackHandler(enabled = selectedChatId != null || showContactProfile != null) {
            if (showContactProfile != null) chatViewModel.showProfile(null)
            else chatViewModel.onBackPress()
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
                        ChatListScreen(
                            chatViewModel = chatViewModel,
                            settingsViewModel = settingsViewModel,
                            calendarViewModel = calendarViewModel
                        )
                    } else {
                        ChatScreen(
                            chatViewModel = chatViewModel,
                            settingsViewModel = settingsViewModel
                        )
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
                            onBack = { chatViewModel.showProfile(null) }
                        )
                    }
                }
            }
        }
    }
}
