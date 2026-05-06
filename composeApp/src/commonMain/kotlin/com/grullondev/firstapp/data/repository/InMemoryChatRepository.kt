package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryChatRepository : ChatRepository {
    private val _chats = MutableStateFlow(
        listOf(
            Chat("1", "Juan Perez", "¡Qué genial! KMP es muy potente.", "10:05 AM", 0),
            Chat("2", "Maria Garcia", "Hola, ¿cómo vas con la app?", "9:30 AM", 2),
            Chat("3", "Android Devs", "Nueva versión de Compose disponible", "Ayer", 0),
            Chat("4", "Mama", "No olvides comprar pan", "Ayer", 1),
            Chat("5", "Trabajo", "Reunión en 10 minutos", "Lunes", 0)
        )
    )

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "1" to listOf(
                ChatMessage("1", "¡Hola! ¿Cómo estás?", false, "10:00 AM"),
                ChatMessage("2", "¡Hola! Todo bien, ¿y tú?", true, "10:01 AM"),
                ChatMessage("3", "Trabajando en un proyecto de KMP.", true, "10:02 AM"),
                ChatMessage("4", "¡Qué genial! KMP es muy potente.", false, "10:05 AM")
            ),
            "2" to listOf(
                ChatMessage("1", "Hola, ¿cómo vas con la app?", false, "9:30 AM")
            )
        )
    )

    override fun getChats(): Flow<List<Chat>> = _chats.asStateFlow()

    override fun getMessages(chatId: String): Flow<List<ChatMessage>> = 
        _messages.asStateFlow().map { it[chatId] ?: emptyList() }

    override suspend fun sendMessage(chatId: String, text: String) {
        val newMessage = ChatMessage(
            id = (_messages.value[chatId]?.size ?: 0).plus(1).toString(),
            text = text,
            isMine = true,
            time = "10:06 AM"
        )
        
        _messages.update { currentMessages ->
            val chatMessages = currentMessages[chatId] ?: emptyList()
            currentMessages + (chatId to (chatMessages + newMessage))
        }

        // Update last message in chat list
        _chats.update { currentChats ->
            currentChats.map { chat ->
                if (chat.id == chatId) {
                    chat.copy(lastMessage = text, lastMessageTime = "Ahora")
                } else chat
            }
        }
    }

    override suspend fun markAllAsRead() {
        _chats.update { currentChats ->
            currentChats.map { it.copy(unreadCount = 0) }
        }
    }
}
