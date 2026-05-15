package com.grullondev.firstapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.model.MessageType

@Composable
fun QuickReplyBar(onQuickReply: (String) -> Unit, themeColor: Color) {
    val quickReplies = listOf("👍", "Ok!", "Ya voy 🚗", "¿Cuándo?", "Gracias! 🙏", "En reunión 📅")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickReplies) { reply ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = themeColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.35f)),
                onClick = { onQuickReply(reply) }
            ) {
                Text(
                    text = reply,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColor
                )
            }
        }
    }
}

@Composable
fun ReplyPreview(message: ChatMessage?, onCancel: () -> Unit, themeColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).background(themeColor).clip(RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message?.senderName ?: "Tú",
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message?.text ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCancel) {
                Text("✕", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendMedia: (MessageType, String?) -> Unit,
    themeColor: Color
) {
    var showMediaMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column {
            if (showMediaMenu) {
                MediaMenu(
                    onAction = { type, name ->
                        onSendMedia(type, name)
                        showMediaMenu = false
                    },
                    themeColor = themeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.safeContentPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showMediaMenu = !showMediaMenu }) {
                            Text("📎", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe tu mensaje...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 4
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = themeColor,
                    onClick = {
                        if (text.isNotBlank()) onSendMessage()
                        else onSendMedia(MessageType.AUDIO, null)
                    },
                    enabled = true
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (text.isNotBlank()) "➤" else "🎤", 
                            color = Color.White, 
                            fontSize = 20.sp, 
                            modifier = Modifier.offset(x = if (text.isNotBlank()) 2.dp else 0.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaMenu(onAction: (MessageType, String?) -> Unit, themeColor: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MediaOption(icon = "🖼️", label = "Galería", onClick = { onAction(MessageType.IMAGE, null) }, color = Color(0xFF9C27B0))
            MediaOption(icon = "📷", label = "Cámara", onClick = { onAction(MessageType.IMAGE, "camera") }, color = Color(0xFFE91E63))
            MediaOption(icon = "📄", label = "Documento", onClick = { onAction(MessageType.FILE, "documento.pdf") }, color = Color(0xFF2196F3))
            MediaOption(icon = "🎵", label = "Audio", onClick = { onAction(MessageType.AUDIO, null) }, color = Color(0xFFFF9800))
        }
    }
}

@Composable
fun MediaOption(icon: String, label: String, onClick: () -> Unit, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
