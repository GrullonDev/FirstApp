package com.grullondev.firstapp.domain.model

enum class CallType { AUDIO, VIDEO }
enum class CallDirection { INCOMING, OUTGOING, MISSED }

data class Call(
    val id: String,
    val contactName: String,
    val type: CallType,
    val direction: CallDirection,
    val time: String,       // "10:30 AM"
    val date: String,       // "Hoy", "Ayer", "Lun 5 May"
    val duration: String?   // null si MISSED, "2:34" si completada
)
