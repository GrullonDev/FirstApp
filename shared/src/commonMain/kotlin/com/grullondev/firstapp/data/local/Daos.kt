package com.grullondev.firstapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("UPDATE chats SET unreadCount = 0")
    suspend fun markAllAsRead()

    @Query("UPDATE chats SET isPinned = NOT isPinned WHERE id = :chatId")
    suspend fun togglePin(chatId: String)
    
    @Query("UPDATE chats SET lastMessage = :text, lastMessageTime = :time, lastMessageType = :type WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, text: String, time: String, type: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY time ASC")
    fun getMessages(chatId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEvent(id: String)
}
