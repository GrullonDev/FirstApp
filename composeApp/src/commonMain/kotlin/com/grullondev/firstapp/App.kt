package com.grullondev.firstapp

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.grullondev.firstapp.data.repository.InMemoryChatRepository
import com.grullondev.firstapp.presentation.ui.ChatListScreen
import com.grullondev.firstapp.presentation.ui.ChatScreen
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel

@Composable
@Preview
fun App() {
    // En una aplicación real, esto se manejaría con Inyección de Dependencias (ej. Koin)
    val repository = remember { InMemoryChatRepository() }
    val viewModel = remember { ChatViewModel(repository) }
    val selectedChatId by viewModel.selectedChatId.collectAsState()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF008069), // WhatsApp Green
            onPrimary = Color.White,
            secondaryContainer = Color.White,
            onSecondaryContainer = Color.Black,
            tertiaryContainer = Color(0xFFE7FFDB), // WhatsApp My Bubble Green
            onTertiaryContainer = Color.Black,
            background = Color(0xFFE5DDD5) // WhatsApp Chat Background
        )
    ) {
        if (selectedChatId == null) {
            ChatListScreen(viewModel = viewModel)
        } else {
            ChatScreen(viewModel = viewModel)
        }
    }
}
