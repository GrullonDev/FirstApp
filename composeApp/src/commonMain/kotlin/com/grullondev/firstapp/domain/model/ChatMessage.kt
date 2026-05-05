package com.grullondev.firstapp.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isMine: Boolean,
    val time: String
)
