package com.example.anonymouschat.ui.screens.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.InitializationState

@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit
) {
    // We assume ChatClient is already initialized elsewhere (e.g. after login)
    val client = ChatClient.instance()
    var isInitialized by remember { mutableStateOf(client.clientState.initializationState.value == InitializationState.COMPLETE) }

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            // For now, we simulate waiting for initialization if it wasn't done globally yet
            // Realistically, the global navigation shouldn't open this until connected
            isInitialized = true
        }
    }

    if (!isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connecting to secure chat...")
        }
        return
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val factory = io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory(
        context = context,
        channelId = "messaging:$chatId",
        messageLimit = 30
    )

    ChatTheme {
        MessagesScreen(
            viewModelFactory = factory,
            onBackPressed = onNavigateBack,
        )
    }
}
