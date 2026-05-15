package com.grullondev.firstapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grullondev.firstapp.domain.model.CalendarEvent
import com.grullondev.firstapp.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalendarViewModel(private val repository: CalendarRepository) : ViewModel() {
    val calendarEvents: StateFlow<List<CalendarEvent>> = repository.getEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch { repository.addEvent(event) }
    }

    fun deleteCalendarEvent(id: String) {
        viewModelScope.launch { repository.deleteEvent(id) }
    }
}
