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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(
    private val chatId: String
) : ViewModel() {
    private val chatRepository = ChatRepository()
    private val authRepository = AuthRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private var currentUserDisplayName: String = "Anonymous"

    init {
        val uid = authRepository.getCurrentUserId()
        _currentUserId.value = uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val docSnapshot = firestore.collection("users").document(uid).get().await()
                    currentUserDisplayName = docSnapshot.getString("displayName") ?: "Anonymous"
                } catch (e: Exception) {
                    // Ignore or log
                }
            }
        }

        viewModelScope.launch {
            chatRepository.getMessages(chatId)
                .catch { e ->
                    // Handle error
                }
                .collect { msgs ->
                    _messages.value = msgs
                }
        }
    }

    fun sendMessage(text: String) {
        val uid = _currentUserId.value ?: return
        if (text.isBlank()) return
        
        viewModelScope.launch {
            val message = Message(
                id = "", 
                chatId = chatId,
                senderId = uid,
                senderName = currentUserDisplayName,
                content = text,
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
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(chatId))
) {
    val messages by viewModel.messages.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
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
                title = { Text(text = chatName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    val isOwnMessage = message.senderId == currentUserId
                    ChatBubble(
                        message = message,
                        isOwnMessage = isOwnMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Surface(
                color = Color.White,
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
