package com.grullondev.firstapp.domain.model

enum class ChatType {
    INDIVIDUAL, GROUP, FAMILY, WORK, TOPIC
}

data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val avatarUrl: String? = null,
    val type: ChatType = ChatType.INDIVIDUAL,
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val typingStatus: String? = null,
    val lastMessageType: MessageType = MessageType.TEXT
)
