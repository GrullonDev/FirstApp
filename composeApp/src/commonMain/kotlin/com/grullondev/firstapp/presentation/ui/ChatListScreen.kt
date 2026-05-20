@file:OptIn(ExperimentalMaterial3Api::class)

package com.grullondev.firstapp.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatType
import com.grullondev.firstapp.domain.model.MessageType
import com.grullondev.firstapp.presentation.ui.theme.LocalAvatarPalette
import com.grullondev.firstapp.presentation.ui.theme.avatarColorFromName
import com.grullondev.firstapp.presentation.viewmodel.CalendarViewModel
import com.grullondev.firstapp.presentation.viewmodel.ChatUiState
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import com.grullondev.firstapp.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    calendarViewModel: CalendarViewModel,
    navController: NavController
) {
    val uiState by chatViewModel.uiState.collectAsState()
    val selectedTab by chatViewModel.selectedTab.collectAsState()
    val themeColor by settingsViewModel.themeColor.collectAsState()
    
    val tabs = listOf(
        Triple("Llamadas", Icons.Outlined.Call, Icons.Default.Call),
        Triple("Chats", Icons.Outlined.Chat, Icons.Default.Chat),
        Triple("Calendario", Icons.Outlined.CalendarMonth, Icons.Default.CalendarMonth),
        Triple("Ajustes", Icons.Outlined.Settings, Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Llamadas"
                                1 -> "Chats"
                                2 -> "Calendario"
                                3 -> "Ajustes"
                                else -> "Chats"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { chatViewModel.onTabSelected(index) },
                        icon = { 
                            Icon(
                                if (selectedTab == index) tab.third else tab.second,
                                contentDescription = tab.first
                            )
                        },
                        label = { Text(tab.first) },
                        alwaysShowLabel = true
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> { // Calls tab
                    FloatingActionButton(
                        onClick = { /* TODO: New Call */ },
                        containerColor = themeColor,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Nueva Llamada")
                    }
                }
                1 -> { // Chats tab
                    FloatingActionButton(
                        onClick = { /* TODO: New Chat */ },
                        containerColor = themeColor,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Text("✏️", fontSize = 22.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> Box(modifier = Modifier.padding(paddingValues)) {
                CallsScreen(themeColor = themeColor)
            }
            2 -> Box(modifier = Modifier.padding(paddingValues)) {
                CalendarScreen(viewModel = calendarViewModel, themeColor = themeColor)
            }
            3 -> Box(modifier = Modifier.padding(paddingValues)) {
                SettingsScreen(settingsViewModel)
            }
            else -> {
                when (val state = uiState) {
                    is ChatUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            LazyColumn {
                                items(8) { SkeletonChatItem() }
                            }
                        }
                    }
                    is ChatUiState.Success -> {
                        ChatListContent(
                            chatViewModel = chatViewModel,
                            settingsViewModel = settingsViewModel,
                            paddingValues = paddingValues,
                            navController = navController,
                            chats = state.chats
                        )
                    }
                    is ChatUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListContent(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    paddingValues: PaddingValues,
    navController: NavController,
    chats: List<Chat>
) {
    val themeColor by settingsViewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by settingsViewModel.isLiquidGlassEnabled.collectAsState()
    val isDarkModeState by settingsViewModel.isDarkMode.collectAsState()
    val onlyUnread by chatViewModel.onlyUnread.collectAsState()
    val isDarkMode = isDarkModeState ?: androidx.compose.foundation.isSystemInDarkTheme()

    var searchQuery by remember { mutableStateOf("") }
    val filteredChats = chats.filter { 
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || 
                          it.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesUnread = !onlyUnread || it.unreadCount > 0
        matchesSearch && matchesUnread
    }

    val pinnedChats = filteredChats.filter { it.isPinned }
    val unpinnedChats = filteredChats.filter { !it.isPinned }
    val groupedChats = unpinnedChats.groupBy { chat ->
        when {
            chat.lastMessageTime.contains("AM") || chat.lastMessageTime.contains("PM") -> "Hoy"
            chat.lastMessageTime == "Ayer" -> "Ayer"
            else -> "Esta semana"
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (isLiquidGlassEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                themeColor.copy(alpha = if (isDarkMode) 0.1f else 0.3f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }

        Column {
            SearchBarBelowAppBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                themeColor = themeColor,
                onlyUnread = onlyUnread,
                onToggleUnread = { chatViewModel.toggleUnreadFilter() }
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (filteredChats.isEmpty()) {
                    item {
                        EmptyChatsState(searchQuery, themeColor)
                    }
                } else {
                    if (searchQuery.isEmpty()) {
                        val favorites = chats.filter { it.type == ChatType.INDIVIDUAL }.take(5)
                        if (favorites.isNotEmpty()) {
                            item {
                                QuickAccessRow(
                                    chats = favorites,
                                    onChatClick = { chatId -> 
                                        navController.navigate("chat_detail/$chatId")
                                    }
                                )
                            }
                        }
                    }

                    // Seccion de Chats Fijados
                    if (pinnedChats.isNotEmpty()) {
                        stickyHeader {
                            HeaderSection("Fijados", themeColor)
                        }
                        items(pinnedChats, key = { "pinned_${it.id}" }) { chat ->
                            ChatItem(
                                chat = chat,
                                onClick = { navController.navigate("chat_detail/${chat.id}") },
                                onLongClick = { chatViewModel.togglePin(chat.id) },
                                isLiquidGlass = isLiquidGlassEnabled,
                                themeColor = themeColor,
                                showPin = true,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    // Secciones por fecha
                    listOf("Hoy", "Ayer", "Esta semana").forEach { day ->
                        val dayChats = groupedChats[day]
                        if (!dayChats.isNullOrEmpty()) {
                            stickyHeader {
                                HeaderSection(day, themeColor)
                            }
                            items(dayChats, key = { it.id }) { chat ->
                                ChatItem(
                                    chat = chat,
                                    onClick = { navController.navigate("chat_detail/${chat.id}") },
                                    onLongClick = { chatViewModel.togglePin(chat.id) },
                                    isLiquidGlass = isLiquidGlassEnabled,
                                    themeColor = themeColor,
                                    modifier = Modifier.animateItem()
                                )
                                if (!isLiquidGlassEnabled) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatsState(searchQuery: String, themeColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(themeColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (searchQuery.isEmpty()) "💬" else "🔍", fontSize = 60.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (searchQuery.isEmpty()) "¡Tu bandeja está tranquila!" else "No encontramos nada para \"$searchQuery\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (searchQuery.isEmpty()) 
                "Inicia una nueva conversación y conecta con el mundo hoy." 
            else "Intenta con otras palabras clave o revisa la ortografía.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HeaderSection(title: String, themeColor: Color) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = themeColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun QuickAccessRow(
    chats: List<Chat>,
    onChatClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Acceso rápido",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chats, key = { it.id }) { chat ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onChatClick(chat.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(avatarColorFromName(chat.name)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.name.take(1),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF3B30))
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Indicador de presencia (punto verde)
                        if (chat.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = chat.name.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    }
}

@Composable
fun SearchBarBelowAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    themeColor: Color,
    onlyUnread: Boolean,
    onToggleUnread: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Buscar...", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        FilterChip(
            selected = onlyUnread,
            onClick = onToggleUnread,
            label = { Text("No leídos", fontSize = 12.sp) },
            leadingIcon = {
                if (onlyUnread) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = themeColor.copy(alpha = 0.2f),
                selectedLabelColor = themeColor
            ),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SkeletonChatItem() {
    val infiniteTransition = rememberInfiniteTransition()
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        ),
        start = androidx.compose.ui.geometry.Offset(xOffset - 200f, 0f),
        end = androidx.compose.ui.geometry.Offset(xOffset, 0f)
    )

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(shimmerBrush))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).background(shimmerBrush))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp).background(shimmerBrush))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isLiquidGlass: Boolean = false,
    themeColor: Color = Color(0xFF008069),
    showPin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val haptic = LocalHapticFeedback.current

    SwipeToDismissBox(
        modifier = modifier.combinedClickable(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            }
        ),
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFFCC00) // Archivar (Amarillo)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF3B30) // Eliminar (Rojo)
                else -> Color.Transparent
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Archive
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> Icons.Default.Clear
            }
            val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        Surface(
            color = if (isLiquidGlass)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
            else
                MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (isLiquidGlass) it.padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                    else it
                },
            shadowElevation = 0.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(
                        when (chat.type) {
                            ChatType.INDIVIDUAL -> avatarColorFromName(chat.name)
                            ChatType.GROUP -> Color(0xFF2196F3).copy(alpha = 0.6f)
                            ChatType.FAMILY -> Color(0xFFFF9800).copy(alpha = 0.6f)
                            ChatType.WORK -> Color(0xFF4CAF50).copy(alpha = 0.6f)
                            ChatType.TOPIC -> Color(0xFF9C27B0).copy(alpha = 0.6f)
                        }
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (chat.type) {
                            ChatType.INDIVIDUAL -> chat.name.take(1)
                            ChatType.GROUP -> "👥"
                            ChatType.FAMILY -> "🏠"
                            ChatType.WORK -> "💼"
                            ChatType.TOPIC -> "💡"
                        },
                        color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    
                    if (showPin) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .align(Alignment.TopStart),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = themeColor
                            )
                        }
                    }

                    // Indicador de presencia en la lista principal
                    if (chat.isOnline && chat.type == ChatType.INDIVIDUAL) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = chat.name, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface
                            )
                            if (chat.type != ChatType.INDIVIDUAL) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = when (chat.type) {
                                            ChatType.GROUP -> "Grupo"; ChatType.FAMILY -> "Familia"
                                            ChatType.WORK -> "Trabajo"; ChatType.TOPIC -> "Temas"
                                            ChatType.INDIVIDUAL -> ""
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = chat.lastMessageTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (chat.unreadCount > 0) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            val typingStatus = chat.typingStatus
                            if (typingStatus != null) {
                                Text(
                                    text = typingStatus,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF25D366),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            } else {
                                // Vista previa de multimedia
                                when (chat.lastMessageType) {
                                    MessageType.IMAGE -> {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    MessageType.AUDIO -> {
                                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    MessageType.FILE -> {
                                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    else -> {}
                                }
                                Text(
                                    text = chat.lastMessage, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (chat.unreadCount > 0) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(themeColor), contentAlignment = Alignment.Center) {
                                Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
