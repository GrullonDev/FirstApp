package com.grullondev.firstapp.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.model.MessageType
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage, 
    themeColor: Color,
    isLiquidGlass: Boolean,
    onLongPress: (String) -> Unit,
    onSwipeReply: (ChatMessage) -> Unit
) {
    val alignment = if (message.isMine) Alignment.CenterEnd else Alignment.CenterStart
    val horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start
    
    val bubbleColor = if (message.isMine) {
        if (isLiquidGlass) themeColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.tertiaryContainer
    } else {
        if (isLiquidGlass) MaterialTheme.colorScheme.surface.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    }

    var offsetX by remember { mutableStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        offsetX += delta
    }

    val isMeetingMessage = message.text.contains("reunion en", ignoreCase = true)
    val isReminderMessage = message.text.contains("no olvides", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.roundToInt().coerceIn(0, 150), 0) }
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (offsetX > 100f) onSwipeReply(message)
                    offsetX = 0f
                }
            ),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = horizontalAlignment) {
            Surface(
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = { onLongPress(message.id) }
                ),
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (message.isMine) 12.dp else 0.dp,
                    bottomEnd = if (message.isMine) 0.dp else 12.dp
                ),
                shadowElevation = if (isLiquidGlass) 0.5.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val senderName = message.senderName
                    if (!message.isMine && senderName != null) {
                        Text(
                            text = senderName,
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 2.dp)
                        )
                    }

                    when (message.type) {
                        MessageType.TEXT -> {
                            val annotatedText = parseMarkdown(message.text, themeColor)
                            val hasUrl = message.text.contains("http")
                            
                            Column {
                                Text(
                                    text = annotatedText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                
                                if (hasUrl) {
                                    LinkPreviewCard(url = "https://kmp.jetbrains.com")
                                }
                            }
                        }
                        MessageType.IMAGE -> {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📷 Imagen", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        MessageType.FILE -> {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📄", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message.fileName ?: "Archivo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        MessageType.AUDIO -> {
                            var isPlaying by remember { mutableStateOf(false) }
                            val audioProgress = remember { Animatable(0f) }
                            
                            LaunchedEffect(isPlaying) {
                                if (isPlaying) {
                                    audioProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
                                    )
                                    isPlaying = false
                                    audioProgress.snapTo(0f)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { isPlaying = !isPlaying }) {
                                    Text(if (isPlaying) "⏸️" else "▶️", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(4.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(audioProgress.value.coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .background(themeColor, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                val seconds = (audioProgress.value * 15).toInt()
                                Text(
                                    text = if (isPlaying) "0:${seconds.toString().padStart(2, '0')}" else "0:15",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (isMeetingMessage) {
                        MeetingActionCard(isMine = message.isMine, themeColor = themeColor)
                    }

                    if (isReminderMessage) {
                        ReminderActionCard(themeColor = themeColor)
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = message.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                        if (message.isMine) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓✓", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LinkPreviewCard(url: String) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Kotlin Multiplatform",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Build apps for Android, iOS, Desktop and Web with a single codebase.",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = url,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
fun MeetingActionCard(isMine: Boolean, themeColor: Color) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Videollamada de grupo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(if (isMine) "Haz clic para iniciar" else "Haz clic para unirte", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text(if (isMine) "Crear" else "Unirse", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ReminderActionCard(themeColor: Color) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Recordatorio", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Agendar esta tarea", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("Recordar", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

fun parseMarkdown(text: String, themeColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("([*_`])(.*?)\\1")
        val matches = regex.findAll(text)
        var lastIndex = 0
        
        for (match in matches) {
            append(text.substring(lastIndex, match.range.first))
            val type = match.groupValues[1]
            val content = match.groupValues[2]
            
            when (type) {
                "*" -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(content) }
                "_" -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(content) }
                "`" -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.3f))) { append(content) }
                else -> append(content)
            }
            lastIndex = match.range.last + 1
        }
        append(text.substring(lastIndex))
    }
}
