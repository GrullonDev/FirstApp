package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.data.local.*
import com.grullondev.firstapp.domain.model.*
import com.grullondev.firstapp.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class RoomChatRepository(private val db: AppDatabase) : ChatRepository {
    override fun getChats(): Flow<List<Chat>> = db.chatDao().getChats()
        .onStart { prepopulateIfEmpty() }
        .map { entities ->
            entities.map { it.toDomain() }
        }

    private fun prepopulateIfEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val chats = listOf(
                ChatEntity("1", "Juan Perez", "¡Qué genial! KMP es muy potente.", "10:05 AM", 0, null, ChatType.INDIVIDUAL, true, true, null, MessageType.TEXT),
                ChatEntity("2", "Maria Garcia", "Hola, ¿cómo vas con la app?", "9:30 AM", 2, null, ChatType.INDIVIDUAL, true, false, "escribiendo...", MessageType.TEXT),
                ChatEntity("3", "Comunidad KMP", "Nueva versión de Compose disponible", "Ayer", 0, null, ChatType.TOPIC, false, false, null, MessageType.TEXT),
                ChatEntity("4", "Familia Rodriguez", "No olvides comprar pan", "Ayer", 1, null, ChatType.FAMILY, true, true, null, MessageType.TEXT)
            )
            db.chatDao().insertChats(chats)
        }
    }

    override fun getMessages(chatId: String): Flow<List<ChatMessage>> = 
        db.messageDao().getMessages(chatId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun sendMessage(chatId: String, text: String, type: MessageType, fileName: String?) {
        val message = ChatMessageEntity(
            id = kotlin.random.Random.nextLong().toString(),
            chatId = chatId,
            text = text,
            isMine = true,
            time = "Ahora",
            senderName = "Tú",
            type = type,
            mediaUrl = null,
            fileName = fileName
        )
        db.messageDao().insertMessage(message)
        db.chatDao().updateLastMessage(chatId, text, "Ahora", type.name)
    }

    override suspend fun markAllAsRead() {
        db.chatDao().markAllAsRead()
    }

    override suspend fun togglePin(chatId: String) {
        db.chatDao().togglePin(chatId)
    }

    // Mappers
    private fun ChatEntity.toDomain() = Chat(
        id = id, name = name, lastMessage = lastMessage, lastMessageTime = lastMessageTime,
        unreadCount = unreadCount, avatarUrl = avatarUrl, type = type, isOnline = isOnline,
        isPinned = isPinned, typingStatus = typingStatus, lastMessageType = lastMessageType
    )

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id, text = text, isMine = isMine, time = time, senderName = senderName,
        type = type, mediaUrl = mediaUrl, fileName = fileName
    )
}
