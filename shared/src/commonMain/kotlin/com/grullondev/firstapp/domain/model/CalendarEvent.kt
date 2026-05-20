package com.grullondev.firstapp.domain.model

enum class EventType { EVENT, REMINDER }

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String = "",
    val date: String,
    val time: String = "09:00",
    val type: EventType = EventType.EVENT
)