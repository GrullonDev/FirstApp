package com.grullondev.firstapp.domain.repository

import com.grullondev.firstapp.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun sendMessage(text: String)
}
