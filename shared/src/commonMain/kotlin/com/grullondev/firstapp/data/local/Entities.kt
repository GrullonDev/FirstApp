package com.grullondev.firstapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grullondev.firstapp.domain.model.ChatType
import com.grullondev.firstapp.domain.model.EventType
import com.grullondev.firstapp.domain.model.MessageType

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val avatarUrl: String?,
    val type: ChatType,
    val isOnline: Boolean,
    val isPinned: Boolean,
    val typingStatus: String?,
    val lastMessageType: MessageType
)

@Entity(tableName = "messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val text: String,
    val isMine: Boolean,
    val time: String,
    val senderName: String?,
    val type: MessageType,
    val mediaUrl: String?,
    val fileName: String?
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val type: EventType
)
