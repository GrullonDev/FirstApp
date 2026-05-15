package com.grullondev.firstapp.domain.repository

import com.grullondev.firstapp.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getEvents(): Flow<List<CalendarEvent>>
    suspend fun addEvent(event: CalendarEvent)
    suspend fun deleteEvent(id: String)
}
