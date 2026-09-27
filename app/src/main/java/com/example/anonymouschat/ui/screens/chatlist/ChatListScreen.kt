package com.example.anonymouschat.ui.screens.chatlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anonymouschat.data.model.Chat
import com.example.anonymouschat.data.repository.AuthRepository
import com.example.anonymouschat.data.repository.ChatRepository
import com.example.anonymouschat.ui.components.AvatarCircle
import com.example.anonymouschat.ui.components.EmptyState
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class ChatListItem(
    val chatId: String,
    val displayName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val chatType: String
)

class ChatListViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val chatRepository = ChatRepository()

    private val _chats = MutableStateFlow<List<ChatListItem>>(emptyList())
    val chats: StateFlow<List<ChatListItem>> = _chats.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        val uid = authRepository.getCurrentUid() ?: return
        viewModelScope.launch {
            chatRepository.getChats(uid).collect { chatModels ->
                val list = chatModels.map { chat ->
                    var displayName = "Unknown"
                    if (chat.type == "direct") {
                        val otherUid = chat.participants.keys.find { it != uid }
                        if (otherUid != null) {
                            try {
                                displayName = FirebaseDatabase.getInstance().getReference("users").child(otherUid).child("displayName")
                                    .get().await().getValue(String::class.java) ?: "Unknown User"
                            } catch (e: Exception) {
                                // Ignore error and use default
                            }
                        }
                    } else {
                        displayName = chat.name ?: "Group Chat"
                    }

                    ChatListItem(
                        chatId = chat.id,
                        displayName = displayName,
                        lastMessage = chat.lastMessage ?: "",
                        lastMessageTime = chat.lastMessageTime ?: 0L,
                        chatType = chat.type
                    )
                }.sortedByDescending { it.lastMessageTime }
                _chats.value = list
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatTapped: (chatId: String, chatName: String) -> Unit,
    onSearchTapped: () -> Unit,
    onSettingsTapped: () -> Unit,
    viewModel: ChatListViewModel = viewModel()
) {
    val chats by viewModel.chats.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Drift", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSearchTapped) {
                        Icon(Icons.Outlined.PersonAdd, contentDescription = "Add/Search")
                    }
                    IconButton(onClick = onSettingsTapped) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSearchTapped) {
                Icon(Icons.Outlined.Chat, contentDescription = "New Message")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (chats.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Chat,
                    title = "No conversations yet",
                    subtitle = "Search for someone to start chatting",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = chats,
                        key = { it.chatId }
                    ) { chat ->
                        ChatListItemRow(
                            chat = chat,
                            onClick = { onChatTapped(chat.chatId, chat.displayName) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItemRow(
    chat: ChatListItem,
    onClick: () -> Unit
) {
    val timeFormatted = remember(chat.lastMessageTime) {
        formatTimestamp(chat.lastMessageTime)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarCircle(
            displayName = chat.displayName,
            size = 52.dp
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = chat.lastMessage.ifEmpty { "New conversation" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val timeCal = Calendar.getInstance().apply { time = date }
    
    return when {
        now.get(Calendar.YEAR) == timeCal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == timeCal.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        now.get(Calendar.YEAR) == timeCal.get(Calendar.YEAR) &&
        now.get(Calendar.WEEK_OF_YEAR) == timeCal.get(Calendar.WEEK_OF_YEAR) -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }
        else -> {
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    }
}
