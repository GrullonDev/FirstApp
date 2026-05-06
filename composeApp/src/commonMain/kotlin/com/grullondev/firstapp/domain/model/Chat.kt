package com.grullondev.firstapp.domain.model

data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val avatarUrl: String? = null
)
