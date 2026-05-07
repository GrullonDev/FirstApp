package com.grullondev.firstapp.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grullondev.firstapp.domain.model.*
import com.grullondev.firstapp.domain.repository.ChatRepository
import com.grullondev.firstapp.domain.repository.PermissionManager
import com.grullondev.firstapp.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val permissionManager: PermissionManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

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

    val themeColor: StateFlow<Color> = settingsRepository.getThemeColor()
        .map { Color(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Color(0xFF008069))

    val isLiquidGlassEnabled = settingsRepository.isLiquidGlassEnabled()
    val isDarkMode = settingsRepository.isDarkMode()

    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    val replyingTo = _replyingTo.asStateFlow()

    private val _selectedTab = MutableStateFlow(2)
    val selectedTab = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()
    private val _privacyLockEnabled = MutableStateFlow(true)
    val privacyLockEnabled = _privacyLockEnabled.asStateFlow()
    private val _saveMediaOnMobileData = MutableStateFlow(false)
    val saveMediaOnMobileData = _saveMediaOnMobileData.asStateFlow()
    private val _useCompactFolders = MutableStateFlow(false)
    val useCompactFolders = _useCompactFolders.asStateFlow()
    private val _selectedLanguage = MutableStateFlow("Español")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _isLoading.value = false
        }
    }

    val selectedChat: StateFlow<Chat?> = combine(chats, _selectedChatId) { chats, id ->
        chats.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onChatSelected(chatId: String?) { _selectedChatId.value = chatId }
    fun onTextChanged(newText: String) { _inputText.value = newText }
    fun onTabSelected(index: Int) { _selectedTab.value = index }

    fun toggleLiquidGlass() {
        viewModelScope.launch { settingsRepository.setLiquidGlassEnabled(!isLiquidGlassEnabled.value) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch { settingsRepository.setDarkMode(!(isDarkMode.value ?: false)) }
    }

    fun onReplyTo(message: ChatMessage?) { _replyingTo.value = message }

    fun updateThemeColor(color: Color) {
        viewModelScope.launch { settingsRepository.setThemeColor(color.value.toLong()) }
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

    fun markAllAsRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }

    fun toggleNotifications() { _notificationsEnabled.value = !_notificationsEnabled.value }
    fun togglePrivacyLock() { _privacyLockEnabled.value = !_privacyLockEnabled.value }
    fun toggleSaveMediaOnMobileData() { _saveMediaOnMobileData.value = !_saveMediaOnMobileData.value }
    fun toggleCompactFolders() { _useCompactFolders.value = !_useCompactFolders.value }
    fun toggleLanguage() {
        _selectedLanguage.value = if (_selectedLanguage.value == "Español") "English" else "Español"
    }

    fun addCalendarEvent(event: CalendarEvent) {
        _calendarEvents.value += event
    }

    fun deleteCalendarEvent(id: String) {
        _calendarEvents.value = _calendarEvents.value.filter { it.id != id }
    }
}