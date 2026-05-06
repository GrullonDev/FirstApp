package com.grullondev.firstapp.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatMessage
import com.grullondev.firstapp.domain.model.MessageType
import com.grullondev.firstapp.domain.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    val chats: StateFlow<List<Chat>> = repository.getChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId = _selectedChatId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _selectedChatId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _themeColor = MutableStateFlow(Color(0xFF008069)) // Default WhatsApp Green
    val themeColor = _themeColor.asStateFlow()

    private val _isLiquidGlassEnabled = MutableStateFlow(false)
    val isLiquidGlassEnabled = _isLiquidGlassEnabled.asStateFlow()

    private val _selectedTab = MutableStateFlow(3) // Default to Chats tab
    val selectedTab = _selectedTab.asStateFlow()

    val selectedChat: StateFlow<Chat?> = combine(chats, _selectedChatId) { chats, id ->
        chats.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onChatSelected(chatId: String?) {
        _selectedChatId.value = chatId
    }

    fun onTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun toggleLiquidGlass() {
        _isLiquidGlassEnabled.value = !_isLiquidGlassEnabled.value
    }

    fun updateThemeColor(color: Color) {
        _themeColor.value = color
    }

    fun sendMessage() {
        val chatId = _selectedChatId.value ?: return
        val text = _inputText.value
        if (text.isNotBlank()) {
            viewModelScope.launch {
                repository.sendMessage(chatId, text)
                _inputText.value = ""
            }
        }
    }

    fun sendMedia(type: MessageType, fileName: String? = null) {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(chatId, "", type, fileName)
        }
    }

    fun onBackPress() {
        _selectedChatId.value = null
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }
}
