package com.grullondev.firstapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.grullondev.firstapp.domain.model.CalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarViewModel : ViewModel() {
    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    fun addCalendarEvent(event: CalendarEvent) {
        _calendarEvents.value += event
    }

    fun deleteCalendarEvent(id: String) {
        _calendarEvents.value = _calendarEvents.value.filter { it.id != id }
    }
}
