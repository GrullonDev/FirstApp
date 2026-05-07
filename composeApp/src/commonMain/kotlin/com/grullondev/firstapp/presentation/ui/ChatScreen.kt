package com.grullondev.firstapp.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.model.MessageType
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val selectedChat by viewModel.selectedChat.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by viewModel.isLiquidGlassEnabled.collectAsState()
    val replyingTo by viewModel.replyingTo.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Calcula si el color es claro u oscuro para elegir texto negro o blanco
    val appBarContentColor = remember(themeColor) {
        val luminance = 0.2126f * themeColor.red + 0.7152f * themeColor.green + 0.0722f * themeColor.blue
        if (luminance > 0.4f) Color.Black else Color.White
    }

    var showCameraSim by remember { mutableStateOf(false) }
    var showReactionMenuFor by remember { mutableStateOf<String?>(null) }

    if (showCameraSim) {
        CameraSimulation(
            onClose = { showCameraSim = false },
            onCapture = { 
                viewModel.sendMedia(MessageType.IMAGE)
                showCameraSim = false
            },
            themeColor = themeColor
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        selectedChat?.let { chat ->
                            ChatHeader(name = chat.name, status = "En línea") 
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onBackPress() }) {
                            Text("←", fontSize = 24.sp, color = appBarContentColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCameraSim = true }) {
                            Text("📹", fontSize = 20.sp, color = appBarContentColor)
                        }
                        IconButton(onClick = { /* Llamada */ }) {
                            Text("📞", fontSize = 20.sp, color = appBarContentColor)
                        }
                        IconButton(onClick = { /* Menú */ }) {
                            Text("⋮", fontSize = 24.sp, color = appBarContentColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = themeColor,
                        scrolledContainerColor = themeColor,
                        navigationIconContentColor = appBarContentColor,
                        titleContentColor = appBarContentColor,
                        actionIconContentColor = appBarContentColor
                    )
                )
            },
            bottomBar = {
                Column(modifier = Modifier.background(Color.Transparent)) {
                    QuickReplyBar(                          // ← AGREGAR
                        onQuickReply = { viewModel.sendQuickReply(it) },
                        themeColor = themeColor
                    )
                    AnimatedVisibility(visible = replyingTo != null) {
                        ReplyPreview(
                            message = replyingTo,
                            onCancel = { viewModel.onReplyTo(null) },
                            themeColor = themeColor
                        )
                    }
                    ChatInput(
                        text = inputText,
                        onTextChange = { viewModel.onTextChanged(it) },
                        onSendMessage = { viewModel.sendMessage() },
                        onSendMedia = { type, name -> 
                            if (type == MessageType.IMAGE && name == "camera") {
                                showCameraSim = true
                            } else {
                                viewModel.sendMedia(type, name)
                            }
                        },
                        themeColor = themeColor
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (isLiquidGlassEnabled) Color.Transparent else MaterialTheme.colorScheme.background)
            ) {
                if (isLiquidGlassEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        themeColor.copy(alpha = if (isDarkMode == true) 0.18f else 0.3f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        Box {
                            MessageBubble(
                                message = message, 
                                themeColor = themeColor, 
                                isLiquidGlass = isLiquidGlassEnabled,
                                onLongPress = { id -> showReactionMenuFor = id },
                                onSwipeReply = { msg -> viewModel.onReplyTo(msg) }
                            )

                            if (showReactionMenuFor == message.id) {
                                ReactionMenu(
                                    onDismiss = { showReactionMenuFor = null },
                                    onReaction = { /* Handle reaction */ showReactionMenuFor = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
fun ReactionMenu(onDismiss: () -> Unit, onReaction: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().clickable { onDismiss() }) {
        Surface(
            modifier = Modifier.padding(8.dp).align(Alignment.TopCenter).offset(y = (-40).dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                listOf("❤️", "😂", "😮", "😢", "🙏", "👍").forEach { emoji ->
                    Text(
                        text = emoji,
                        modifier = Modifier
                            .clickable { onReaction(emoji) }
                            .padding(horizontal = 8.dp),
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatHeader(name: String, status: String, contentColor: Color = Color.White) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1), color = contentColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Text(status, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
        }
    }
}

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
                    if (!message.isMine && message.senderName != null) {
                        Text(
                            text = message.senderName,
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
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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

fun parseMarkdown(text: String, themeColor: Color): AnnotatedString {
    return buildAnnotatedString {
        // Simple parsing logic for *bold*, _italic_, `code`
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

@Composable
fun MeetingActionCard(isMine: Boolean, themeColor: Color) {
// ...
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
