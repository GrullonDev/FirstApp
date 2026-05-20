package com.grullondev.firstapp

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grullondev.firstapp.presentation.ui.*
import com.grullondev.firstapp.presentation.ui.theme.AppTheme
import com.grullondev.firstapp.presentation.viewmodel.CalendarViewModel
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import com.grullondev.firstapp.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable data object ChatList : Screen()
    @Serializable data class ChatDetail(val chatId: String) : Screen()
    @Serializable data class ContactProfile(val chatId: String) : Screen()
}

@Composable
fun App() {
    val chatViewModel: ChatViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val calendarViewModel: CalendarViewModel = koinViewModel()
    
    val userDarkModePref by settingsViewModel.isDarkMode.collectAsState()
    val themeColor by settingsViewModel.themeColor.collectAsState()
    val chats by chatViewModel.chats.collectAsState()

    val navController = rememberNavController()

    AppTheme(
        userDarkModePref = userDarkModePref,
        themeColor = themeColor
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = navController,
                startDestination = "chat_list"
            ) {
                composable("chat_list") {
                    ChatListScreen(
                        chatViewModel = chatViewModel,
                        settingsViewModel = settingsViewModel,
                        calendarViewModel = calendarViewModel,
                        navController = navController
                    )
                }
                
                composable("chat_detail/{chatId}") { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId")
                    if (chatId != null) {
                        LaunchedEffect(chatId) {
                            chatViewModel.onChatSelected(chatId)
                        }
                        ChatScreen(
                            chatViewModel = chatViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }

                composable("contact_profile/{chatId}") { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId")
                    val chat = chats.find { it.id == chatId }
                    if (chat != null) {
                        ContactProfileScreen(
                            chat = chat,
                            themeColor = themeColor,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
