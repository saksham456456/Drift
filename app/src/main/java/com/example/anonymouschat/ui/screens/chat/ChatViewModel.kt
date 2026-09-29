package com.example.anonymouschat.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.anonymouschat.data.model.Message
import com.example.anonymouschat.data.repository.AuthRepository
import com.example.anonymouschat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatViewModel(private val chatId: String) : ViewModel() {
    private val chatRepository = ChatRepository()
    private val authRepository = AuthRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _chatName = MutableStateFlow("")
    val chatName: StateFlow<String> = _chatName.asStateFlow()

    private val _chatType = MutableStateFlow("direct")
    val chatType: StateFlow<String> = _chatType.asStateFlow()

    private val _otherUserStatus = MutableStateFlow("")
    val otherUserStatus: StateFlow<String> = _otherUserStatus.asStateFlow()

    private var currentUserDisplayName: String = "Anonymous"

    init {
        loadCurrentUser()
        loadChatDetails()
        loadMessages()
    }

    private fun loadCurrentUser() {
        val uid = authRepository.getCurrentUid()
        _currentUserId.value = uid
        if (uid != null) {
            viewModelScope.launch {
                chatRepository.getUserProfile(uid).collectLatest { user ->
                    if (user != null) {
                        currentUserDisplayName = user.displayName
                    }
                }
            }
        }
    }

    private fun loadChatDetails() {
        viewModelScope.launch {
            chatRepository.getChat(chatId).collectLatest { chat ->
                if (chat != null) {
                    _chatType.value = chat.type
                    if (chat.type == "group") {
                        _chatName.value = chat.name
                        _otherUserStatus.value = "${chat.participants.size} members"
                    } else {
                        // Direct chat: find the other participant
                        val currentUid = _currentUserId.value
                        val otherUid = chat.participants.keys.find { it != currentUid }
                        if (otherUid != null) {
                            observeOtherUserProfileAndStatus(otherUid)
                        }
                    }
                }
            }
        }
    }

    private fun observeOtherUserProfileAndStatus(otherUid: String) {
        // Load name
        viewModelScope.launch {
            chatRepository.getUserProfile(otherUid).collectLatest { user ->
                if (user != null) {
                    _chatName.value = user.displayName
                }
            }
        }
        
        // Load presence status cleanly using Flow (auto-cancels on scope exit!)
        viewModelScope.launch {
            chatRepository.getOtherUserStatus(otherUid).collectLatest { lastSeen ->
                if (lastSeen == null) {
                    _otherUserStatus.value = "Offline"
                } else if (lastSeen == -1L) {
                    _otherUserStatus.value = "Online"
                } else {
                    val date = Date(lastSeen)
                    val format = if (isToday(date)) {
                        SimpleDateFormat("'today at' HH:mm", Locale.getDefault())
                    } else {
                        SimpleDateFormat("MMM dd", Locale.getDefault())
                    }
                    _otherUserStatus.value = "Last seen ${format.format(date)}"
                }
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(chatId)
                .catch { e ->
                    // Handle error
                }
                .collectLatest { msgs ->
                    _messages.value = msgs
                    _currentUserId.value?.let { uid ->
                        chatRepository.markChatRead(chatId, uid)
                    }
                }
        }
    }

    private fun isToday(date: Date): Boolean {
        val now = Calendar.getInstance()
        val timeCal = Calendar.getInstance().apply { time = date }
        return now.get(Calendar.YEAR) == timeCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == timeCal.get(Calendar.DAY_OF_YEAR)
    }

    fun sendMessage(text: String) {
        val uid = _currentUserId.value ?: return
        if (text.isBlank()) return
        
        viewModelScope.launch {
            val message = Message(
                id = "", 
                text = text,
                senderUid = uid,
                senderName = currentUserDisplayName,
                timestamp = System.currentTimeMillis()
            )
            chatRepository.sendMessage(chatId, message)
        }
    }
}

class ChatViewModelFactory(private val chatId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
