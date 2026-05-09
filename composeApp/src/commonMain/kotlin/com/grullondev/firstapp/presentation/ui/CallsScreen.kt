package com.grullondev.firstapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.Call
import com.grullondev.firstapp.domain.model.CallDirection
import com.grullondev.firstapp.domain.model.CallType
import kotlin.math.absoluteValue

private fun getAvatarColor(name: String): Color {
    val avatarColors = listOf(
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
        Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF29B6F6), Color(0xFF26C6DA),
        Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157),
        Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFFA726), Color(0xFFFF7043)
    )
    return avatarColors[name.hashCode().absoluteValue % avatarColors.size]
}

@Composable
fun CallsScreen(themeColor: Color) {
    val calls = remember { sampleCalls() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(calls, key = { it.id }) { call ->
                CallItem(call = call, themeColor = themeColor)
                HorizontalDivider(
                    modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                )
            }
        }
    }
}

@Composable
fun CallItem(call: Call, themeColor: Color) {
    val directionColor = if (call.direction == CallDirection.MISSED)
        Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape)
                .background(getAvatarColor(call.contactName)),
            contentAlignment = Alignment.Center
        ) {
            Text(call.contactName.take(1), color = Color.White,
                fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(call.contactName,
                style = MaterialTheme.typography.titleMedium,
                color = directionColor, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Flecha dirección
                Text(
                    text = when (call.direction) {
                        CallDirection.OUTGOING -> "↗"
                        CallDirection.INCOMING -> "↙"
                        CallDirection.MISSED   -> "↙"
                    },
                    color = directionColor, fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${call.date} · ${call.time}" +
                        (call.duration?.let { " · $it" } ?: " · Perdida"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Icono tipo llamada (audio/video) para llamar de nuevo
        IconButton(onClick = { }) {
            Icon(
                imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = null,
                tint = themeColor
            )
        }
    }
}

fun sampleCalls(): List<Call> = listOf(
    Call("1", "Juan Perez",    CallType.AUDIO, CallDirection.OUTGOING, "10:30 AM", "Hoy",  "2:34"),
    Call("2", "Maria Garcia",  CallType.VIDEO, CallDirection.MISSED,   "9:15 AM",  "Hoy",  null),
    Call("3", "Juan Perez",    CallType.AUDIO, CallDirection.INCOMING, "8:00 PM",  "Ayer", "0:45"),
    Call("4", "Proyecto App",  CallType.VIDEO, CallDirection.OUTGOING, "4:00 PM",  "Ayer", "15:20"),
    Call("5", "Maria Garcia",  CallType.AUDIO, CallDirection.INCOMING, "11:00 AM", "Lun",  "3:10"),
)
