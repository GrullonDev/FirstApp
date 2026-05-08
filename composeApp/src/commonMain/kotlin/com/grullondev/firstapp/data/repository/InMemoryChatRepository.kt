package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatType
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.model.MessageType
import com.grullondev.firstapp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryChatRepository : ChatRepository {
    private val _chats = MutableStateFlow(
        listOf(
            Chat("1", "Juan Perez", "¡Qué genial! KMP es muy potente.", "10:05 AM", 0, type = ChatType.INDIVIDUAL, isOnline = true, isPinned = true),
            Chat("2", "Maria Garcia", "Hola, ¿cómo vas con la app?", "9:30 AM", 2, type = ChatType.INDIVIDUAL, isOnline = true, typingStatus = "escribiendo..."),
            Chat("3", "Comunidad KMP", "Nueva versión de Compose disponible", "Ayer", 0, type = ChatType.TOPIC),
            Chat("4", "Familia Rodriguez", "No olvides comprar pan", "Ayer", 1, type = ChatType.FAMILY, isOnline = true, isPinned = true),
            Chat("5", "Proyecto App", "Reunión en 10 minutos para revisar el avance.", "10:10 AM", 0, type = ChatType.WORK),
            Chat("6", "Carlos Ruiz", "Foto", "11:20 AM", 0, type = ChatType.INDIVIDUAL, lastMessageType = MessageType.IMAGE),
            Chat("7", "Ana Lopez", "Audio", "9:15 AM", 1, type = ChatType.INDIVIDUAL, isOnline = true, lastMessageType = MessageType.AUDIO)
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
            ),
            "3" to listOf(
                ChatMessage("1", "Nueva versión de Compose disponible", false, "Ayer", senderName = "Google Admin")
            ),
            "4" to listOf(
                ChatMessage("1", "No olvides comprar pan", false, "Ayer", senderName = "Mama")
            ),
            "5" to listOf(
                ChatMessage("1", "Hola equipo, tenemos temas pendientes.", false, "10:00 AM", senderName = "Carlos"),
                ChatMessage("2", "Reunion en 10 minutos para revisar el avance.", false, "10:10 AM", senderName = "Ana")
            )
        )
    )

    override fun getChats(): Flow<List<Chat>> = _chats.asStateFlow()

    override fun getMessages(chatId: String): Flow<List<ChatMessage>> = 
        _messages.asStateFlow().map { it[chatId] ?: emptyList() }

    override suspend fun sendMessage(chatId: String, text: String, type: MessageType, fileName: String?) {
        val newMessage = ChatMessage(
            id = (_messages.value[chatId]?.size ?: 0).plus(1).toString(),
            text = text,
            isMine = true,
            time = "Ahora",
            type = type,
            fileName = fileName
        )
        
        _messages.update { currentMessages ->
            val chatMessages = currentMessages[chatId] ?: emptyList()
            currentMessages + (chatId to (chatMessages + newMessage))
        }

        // Update last message in chat list
        _chats.update { currentChats ->
            currentChats.map { chat ->
                if (chat.id == chatId) {
                    val lastMsg = when(type) {
                        MessageType.TEXT -> text
                        MessageType.IMAGE -> "📷 Imagen"
                        MessageType.FILE -> "📄 Archivo: $fileName"
                        MessageType.AUDIO -> "🎤 Audio"
                    }
                    chat.copy(lastMessage = lastMsg, lastMessageTime = "Ahora")
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
