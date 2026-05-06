package com.grullondev.firstapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatMessage
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

    val selectedChat: StateFlow<Chat?> = combine(chats, _selectedChatId) { chats, id ->
        chats.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onChatSelected(chatId: String?) {
        _selectedChatId.value = chatId
    }

    fun onTextChanged(newText: String) {
        _inputText.value = newText
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

    fun onBackPress() {
        _selectedChatId.value = null
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }
}
