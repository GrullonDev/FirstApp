package com.grullondev.firstapp.data.local

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.grullondev.firstapp.domain.model.ChatType
import com.grullondev.firstapp.domain.model.EventType
import com.grullondev.firstapp.domain.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [ChatEntity::class, ChatMessageEntity::class, CalendarEventEntity::class],
    version = 1
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun calendarDao(): CalendarDao
}

class AppTypeConverters {
    @TypeConverter
    fun fromChatType(value: ChatType) = value.name
    @TypeConverter
    fun toChatType(value: String) = ChatType.valueOf(value)

    @TypeConverter
    fun fromMessageType(value: MessageType) = value.name
    @TypeConverter
    fun toMessageType(value: String) = MessageType.valueOf(value)

    @TypeConverter
    fun fromEventType(value: EventType) = value.name
    @TypeConverter
    fun toEventType(value: String) = EventType.valueOf(value)
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
