package com.example.anonymouschat.feature_pocket.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.anonymouschat.core.ui.OxygenRing

/**
 * The Pocket — a private, ephemeral conversation space.
 *
 * Displays the chat messages, the oxygen ring in the top bar,
 * and a message input at the bottom. When oxygen depletes,
 * the pocket dissolves and offers a Constellation prompt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketScreen(driftId: String,
    onNavigateBack: () -> Unit,
    viewModel: PocketViewModel = viewModel(factory = PocketViewModel.Factory(driftId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "pocket",
                            fontWeight = FontWeight.Thin,
                            fontSize = 20.sp,
                            letterSpacing = 4.sp,
                            color = Color(0xFFE0E0E0)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OxygenRing(progress = state.oxygenProgress)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave pocket",
                            tint = Color(0xFF00E676)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Origin drift that started this pocket
            if (state.originDriftText.isNotBlank()) {
                Text(
                    text = "\"${state.originDriftText}\"",
                    color = Color(0xFF00E676).copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = Color(0xFF222222))
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    val isMe = message.senderId == "local_user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) Color(0xFF2E7D32) else Color(0xFF1E1E1E)
                            )
                        ) {
                            Text(
                                text = message.text,
                                color = Color(0xFFE0E0E0),
                                fontSize = 15.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Dissolved state
            if (state.isDissolved) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "this pocket has dissolved",
                        color = Color(0xFF00E676).copy(alpha = 0.6f),
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Message input
            if (!state.isDissolved) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121212))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("say something...", color = Color.Gray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFFE0E0E0),
                            unfocusedTextColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF333333),
                            cursorColor = Color(0xFF00E676)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.onEvent(PocketEvent.SendMessage(messageText))
                            messageText = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF00E676), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF0A0A0A)
                        )
                    }
                }
            }
        }
    }

    // Constellation prompt dialog
    if (state.showConstellationPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(PocketEvent.DeclineConstellation) },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text(
                    "form a constellation? ✦",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
            },
            text = {
                Text(
                    "If you both agree, you'll get one more chance to find each other. After that, the star fades forever.",
                    color = Color(0xFFE0E0E0).copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(PocketEvent.FormConstellation) }) {
                    Text("form a star ✦", color = Color(0xFF00E676))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(PocketEvent.DeclineConstellation) }) {
                    Text("let it fade", color = Color.Gray)
                }
            }
        )
    }
}
