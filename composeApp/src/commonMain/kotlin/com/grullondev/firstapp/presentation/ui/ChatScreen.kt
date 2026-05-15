package com.grullondev.firstapp.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.ChatType
import com.grullondev.firstapp.domain.model.MessageType
import com.grullondev.firstapp.presentation.ui.components.*
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import com.grullondev.firstapp.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel
) {
    val messages by chatViewModel.messages.collectAsState()
    val inputText by chatViewModel.inputText.collectAsState()
    val selectedChat by chatViewModel.selectedChat.collectAsState()
    val themeColor by settingsViewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by settingsViewModel.isLiquidGlassEnabled.collectAsState()
    val replyingTo by chatViewModel.replyingTo.collectAsState()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Calcula si el color es claro u oscuro para elegir texto negro o blanco
    val appBarContentColor = remember(themeColor) {
        val luminance = 0.2126f * themeColor.red + 0.7152f * themeColor.green + 0.0722f * themeColor.blue
        if (luminance > 0.4f) Color.Black else Color.White
    }

    var showCameraSim by remember { mutableStateOf(false) }
    var showInCallScreen by remember { mutableStateOf(false) }
    var isVideoCall by remember { mutableStateOf(false) }
    var showReactionMenuFor by remember { mutableStateOf<String?>(null) }

    if (showCameraSim) {
        CameraSimulation(
            onClose = { showCameraSim = false },
            onCapture = { 
                chatViewModel.sendMedia(MessageType.IMAGE)
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
                            val status = when (chat.type) {
                                ChatType.INDIVIDUAL -> if (chat.typingStatus != null) chat.typingStatus!! else "En línea"
                                ChatType.GROUP      -> "${chat.name.length % 3 + 2} participantes"
                                ChatType.FAMILY     -> "5 participantes"
                                ChatType.WORK       -> "4 participantes"
                                ChatType.TOPIC      -> "Tema activo"
                            }
                            ChatHeader(
                                name = chat.name, 
                                status = status,
                                contentColor = appBarContentColor,
                                onClick = { chatViewModel.showProfile(chat.id) }
                            ) 
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { chatViewModel.onBackPress() }) {
                            Text("←", fontSize = 24.sp, color = appBarContentColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCameraSim = true }) {
                            Text("📹", fontSize = 20.sp, color = appBarContentColor)
                        }
                        IconButton(onClick = { isVideoCall = true; showInCallScreen = true }) {
                            Text("📹", fontSize = 20.sp, color = appBarContentColor)
                        }
                        IconButton(onClick = { isVideoCall = false; showInCallScreen = true }) {
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
                    AnimatedVisibility(
                        visible = inputText.isEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        QuickReplyBar(
                            onQuickReply = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                chatViewModel.sendQuickReply(it) 
                            },
                            themeColor = themeColor
                        )
                    }
                    AnimatedVisibility(visible = replyingTo != null) {
                        ReplyPreview(
                            message = replyingTo,
                            onCancel = { chatViewModel.onReplyTo(null) },
                            themeColor = themeColor
                        )
                    }
                    ChatInput(
                        text = inputText,
                        onTextChange = { chatViewModel.onTextChanged(it) },
                        onSendMessage = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            chatViewModel.sendMessage() 
                        },
                        onSendMedia = { type, name -> 
                            if (type == MessageType.IMAGE && name == "camera") {
                                showCameraSim = true
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                chatViewModel.sendMedia(type, name)
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
                                onSwipeReply = { msg -> chatViewModel.onReplyTo(msg) }
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

    if (showInCallScreen) {
        CallDialog(
            selectedChatName = selectedChat?.name,
            themeColor = themeColor,
            isVideoCall = isVideoCall,
            onClose = { showInCallScreen = false }
        )
    }
}

@Composable
fun CallDialog(selectedChatName: String?, themeColor: Color, isVideoCall: Boolean, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = Color(0xFF1A1A2E),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape)
                        .background(themeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedChatName?.take(1) ?: "?",
                        color = Color.White, fontSize = 36.sp,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(selectedChatName ?: "", color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text(if (isVideoCall) "Videollamada..." else "Llamando...",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón silenciar
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { },
                            contentAlignment = Alignment.Center) {
                            Text("🔇", fontSize = 22.sp)
                        }
                        Text("Silencio", color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                    // Botón colgar (centro, rojo)
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(68.dp).clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .clickable { onClose() },
                            contentAlignment = Alignment.Center) {
                            Text("📵", fontSize = 26.sp)
                        }
                        Text("Colgar", color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                    // Botón altavoz
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { },
                            contentAlignment = Alignment.Center) {
                            Text("🔊", fontSize = 22.sp)
                        }
                        Text("Altavoz", color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        },
        confirmButton = {}
    )
}
