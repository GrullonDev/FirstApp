package com.grullondev.firstapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.model.MessageType
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val selectedChat by viewModel.selectedChat.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by viewModel.isLiquidGlassEnabled.collectAsState()

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
                        Text("←", fontSize = 24.sp, color = Color.White)
                    }
                },
                actions = {
                    Text("📹", modifier = Modifier.padding(horizontal = 8.dp), fontSize = 20.sp)
                    Text("📞", modifier = Modifier.padding(horizontal = 8.dp), fontSize = 20.sp)
                    Text("⋮", modifier = Modifier.padding(horizontal = 8.dp), fontSize = 24.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isLiquidGlassEnabled) themeColor.copy(alpha = 0.8f) else themeColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatInput(
                text = inputText,
                onTextChange = { viewModel.onTextChanged(it) },
                onSendMessage = { viewModel.sendMessage() },
                onSendMedia = { type, name -> viewModel.sendMedia(type, name) },
                themeColor = themeColor
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (isLiquidGlassEnabled) Color.Transparent else Color(0xFFE5DDD5))
        ) {
            if (isLiquidGlassEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(themeColor.copy(alpha = 0.2f), Color.White)
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
                    MessageBubble(message, themeColor, isLiquidGlassEnabled)
                }
            }
        }
    }
}

@Composable
fun ChatHeader(name: String, status: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(status, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage, 
    themeColor: Color,
    isLiquidGlass: Boolean
) {
    val alignment = if (message.isMine) Alignment.CenterEnd else Alignment.CenterStart
    val horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isMine) {
        if (isLiquidGlass) themeColor.copy(alpha = 0.4f) else Color(0xFFE7FFDB)
    } else {
        if (isLiquidGlass) Color.White.copy(alpha = 0.4f) else Color.White
    }
    
    val isMeetingMessage = message.text.contains("reunion en", ignoreCase = true)
    val isReminderMessage = message.text.contains("no olvides", ignoreCase = true)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = horizontalAlignment) {
            Surface(
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
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }
                        MessageType.IMAGE -> {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📷 Imagen", color = Color.Gray)
                            }
                        }
                        MessageType.FILE -> {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📄", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message.fileName ?: "Archivo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        MessageType.AUDIO -> {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("▶️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(2.dp)
                                        .background(themeColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("0:15", style = MaterialTheme.typography.labelSmall)
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
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        if (message.isMine) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓✓", color = Color(0xFF34B7F1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingActionCard(isMine: Boolean, themeColor: Color) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Videollamada de grupo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(if (isMine) "Haz clic para iniciar" else "Haz clic para unirte", style = MaterialTheme.typography.labelSmall)
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Recordatorio", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Agendar esta tarea", style = MaterialTheme.typography.labelSmall)
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
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showMediaMenu = !showMediaMenu }) {
                            Text("📎", fontSize = 22.sp)
                        }
                        TextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe tu mensaje...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
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
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MediaOption(icon = "🖼️", label = "Galería", onClick = { onAction(MessageType.IMAGE, null) }, color = Color(0xFF9C27B0))
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
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
