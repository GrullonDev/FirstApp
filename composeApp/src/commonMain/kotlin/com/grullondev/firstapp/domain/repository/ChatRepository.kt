package com.grullondev.firstapp.domain.repository

import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, text: String)
    suspend fun markAllAsRead()
}
