package com.grullondev.firstapp.domain.model

enum class MessageType {
    TEXT, IMAGE, FILE, AUDIO
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isMine: Boolean,
    val time: String,
    val senderName: String? = null,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val fileName: String? = null
)
