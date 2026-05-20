package com.grullondev.firstapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grullondev.firstapp.domain.model.*
import com.grullondev.firstapp.domain.repository.ChatRepository
import com.grullondev.firstapp.domain.repository.PermissionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ChatUiState {
    data object Loading : ChatUiState()
    data class Success(val chats: List<Chat>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(
    private val repository: ChatRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    val uiState: StateFlow<ChatUiState> = repository.getChats()
        .map { chats -> 
            if (chats.isEmpty()) ChatUiState.Loading 
            else ChatUiState.Success(chats) 
        }
        .catch { emit(ChatUiState.Error(it.message ?: "Error desconocido")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState.Loading)

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

    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    val replyingTo = _replyingTo.asStateFlow()

    private val _selectedTab = MutableStateFlow(1) // Default to Chats tab
    val selectedTab = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _onlyUnread = MutableStateFlow(false)
    val onlyUnread = _onlyUnread.asStateFlow()

    private val _showContactProfile = MutableStateFlow<String?>(null)
    val showContactProfile = _showContactProfile.asStateFlow()

    val selectedChat: StateFlow<Chat?> = combine(chats, _selectedChatId) { chats, id ->
        chats.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onChatSelected(chatId: String?) { _selectedChatId.value = chatId }
    fun onTextChanged(newText: String) { _inputText.value = newText }
    fun onTabSelected(index: Int) { _selectedTab.value = index }

    fun onReplyTo(message: ChatMessage?) { _replyingTo.value = message }

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

    fun sendQuickReply(text: String) {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch { repository.sendMessage(chatId, text) }
    }

    fun sendMedia(type: MessageType, fileName: String? = null) {
        val chatId = _selectedChatId.value ?: return
        val permissionRequired = when(type) {
            MessageType.IMAGE -> Permission.GALLERY
            MessageType.FILE -> Permission.FILE_STORAGE
            MessageType.AUDIO -> Permission.RECORD_AUDIO
            else -> null
        }
        viewModelScope.launch {
            val isGranted = if (permissionRequired != null) {
                permissionManager.requestPermission(permissionRequired) == PermissionState.GRANTED
            } else true
            if (isGranted) repository.sendMessage(chatId, "", type, fileName)
        }
    }

    fun onBackPress() { _selectedChatId.value = null }

    fun togglePin(chatId: String) {
        viewModelScope.launch {
            repository.togglePin(chatId)
        }
    }

    fun toggleUnreadFilter() {
        _onlyUnread.value = !_onlyUnread.value
    }

    fun showProfile(chatId: String?) {
        _showContactProfile.value = chatId
    }

    fun markAllAsRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }
}
