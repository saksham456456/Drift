package com.example.anonymouschat.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anonymouschat.data.model.Message
import com.example.anonymouschat.data.repository.AuthRepository
import com.example.anonymouschat.data.repository.ChatRepository
import com.example.anonymouschat.ui.components.ChatBubble
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

class ChatViewModel(
    private val chatId: String
) : ViewModel() {
    private val chatRepository = ChatRepository()
    private val authRepository = AuthRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _otherUserStatus = MutableStateFlow<String>("")
    val otherUserStatus: StateFlow<String> = _otherUserStatus.asStateFlow()

    private var currentUserDisplayName: String = "Anonymous"

    init {
        val uid = authRepository.getCurrentUid()
        _currentUserId.value = uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    currentUserDisplayName = FirebaseDatabase.getInstance().getReference("users").child(uid).child("displayName")
                        .get().await().getValue(String::class.java) ?: "Anonymous"
                } catch (e: Exception) {
                    // Ignore or log
                }
            }
            
            // Fetch other user status
            viewModelScope.launch {
                try {
                    val chatSnapshot = FirebaseDatabase.getInstance().getReference("chats/$chatId").get().await()
                    val chat = chatSnapshot.getValue(com.example.anonymouschat.data.model.Chat::class.java)
                    if (chat != null && chat.type == "direct") {
                        val otherUid = chat.participants.keys.find { it != uid }
                        if (otherUid != null) {
                            FirebaseDatabase.getInstance().getReference("users/$otherUid/lastSeen")
                                .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                        val lastSeen = snapshot.getValue(Long::class.java) ?: 0L
                                        if (lastSeen == -1L) {
                                            _otherUserStatus.value = "Online"
                                        } else if (lastSeen > 0L) {
                                            val date = java.util.Date(lastSeen)
                                            val format = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                            _otherUserStatus.value = "Last seen ${format.format(date)}"
                                        } else {
                                            _otherUserStatus.value = "Offline"
                                        }
                                    }
                                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                                })
                        }
                    }
                } catch (e: Exception) {}
            }
        }

        viewModelScope.launch {
            chatRepository.getMessages(chatId)
                .catch { e ->
                    // Handle error
                }
                .collect { msgs ->
                    _messages.value = msgs
                    _currentUserId.value?.let { uid ->
                        chatRepository.markChatRead(chatId, uid)
                    }
                }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    chatName: String,
    chatType: String = "direct",
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(chatId))
) {
    val messages by viewModel.messages.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val otherUserStatus by viewModel.otherUserStatus.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(text = chatName)
                        if (otherUserStatus.isNotEmpty()) {
                            Text(
                                text = otherUserStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (otherUserStatus == "Online") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id.ifEmpty { it.timestamp.toString() } }
                ) { message ->
                    val isOwnMessage = message.senderUid == currentUserId
                    ChatBubble(
                        message = message.text,
                        senderName = message.senderName,
                        isOwnMessage = isOwnMessage,
                        timestamp = message.timestamp,
                        showSenderName = chatType == "group",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText.trim())
                                    inputText = ""
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message"
                        )
                    }
                }
            }
        }
    }
}
