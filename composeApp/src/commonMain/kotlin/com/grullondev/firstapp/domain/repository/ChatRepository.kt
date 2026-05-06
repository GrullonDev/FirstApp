package com.grullondev.firstapp.domain.repository

import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.model.MessageType
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, text: String, type: MessageType = MessageType.TEXT, fileName: String? = null)
    suspend fun markAllAsRead()
}
