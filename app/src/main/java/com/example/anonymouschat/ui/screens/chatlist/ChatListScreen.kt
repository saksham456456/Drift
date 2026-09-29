package com.example.anonymouschat.ui.screens.chatlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.client.ChatClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatTapped: (String, String) -> Unit,
    onSearchTapped: () -> Unit,
    onCreateGroupTapped: () -> Unit,
    onSettingsTapped: () -> Unit
) {
    val client = ChatClient.instance()
    var isInitialized by remember { mutableStateOf(client.clientState.initializationState.value == InitializationState.COMPLETE) }

    LaunchedEffect(client.clientState.initializationState) {
        client.clientState.initializationState.collect { state ->
            isInitialized = (state == InitializationState.COMPLETE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drift") },
                actions = {
                    IconButton(onClick = onCreateGroupTapped) {
                        Icon(Icons.Outlined.GroupAdd, contentDescription = "Create Group")
                    }
                    IconButton(onClick = onSearchTapped) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onSettingsTapped) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isInitialized) {
                ChatTheme {
                    ChannelsScreen(
                        title = "Chats",
                        isShowingHeader = false,
                        onItemClick = { channel -> 
                            val rawId = channel.cid.replace("messaging:", "")
                            val name = channel.name ?: "Chat"
                            onChatTapped(rawId, name) 
                        },
                        onBackPressed = { /* Desktop back handled by OS */ }
                    )
                }
            } else {
                Text(
                    text = "Connecting...",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
