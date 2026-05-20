package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.data.local.AppDatabase
import com.grullondev.firstapp.data.local.CalendarEventEntity
import com.grullondev.firstapp.domain.model.CalendarEvent
import com.grullondev.firstapp.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCalendarRepository(private val db: AppDatabase) : CalendarRepository {
    override fun getEvents(): Flow<List<CalendarEvent>> = db.calendarDao().getEvents().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun addEvent(event: CalendarEvent) {
        db.calendarDao().insertEvent(event.toEntity())
    }

    override suspend fun deleteEvent(id: String) {
        db.calendarDao().deleteEvent(id)
    }

    private fun CalendarEventEntity.toDomain() = CalendarEvent(
        id = id, title = title, description = description, date = date, time = time, type = type
    )

    private fun CalendarEvent.toEntity() = CalendarEventEntity(
        id = id, title = title, description = description, date = date, time = time, type = type
    )
}
