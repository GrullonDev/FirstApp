package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryChatRepository : ChatRepository {
    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage("1", "¡Hola! ¿Cómo estás?", false, "10:00 AM"),
            ChatMessage("2", "¡Hola! Todo bien, ¿y tú?", true, "10:01 AM"),
            ChatMessage("3", "Trabajando en un proyecto de KMP.", true, "10:02 AM"),
            ChatMessage("4", "¡Qué genial! KMP es muy potente.", false, "10:05 AM")
        )
    )

    override fun getMessages(): Flow<List<ChatMessage>> = _messages.asStateFlow()

    override suspend fun sendMessage(text: String) {
        val newMessage = ChatMessage(
            id = (_messages.value.size + 1).toString(),
            text = text,
            isMine = true,
            time = "10:06 AM" // En una app real, usaríamos un formateador de fecha
        )
        _messages.update { it + newMessage }
    }
}
