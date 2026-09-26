package com.example.anonymouschat.feature_ocean.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.anonymouschat.core.ui.DriftBubble

/**
 * The Ocean — the heart of Drift.
 *
 * A dark, ambient screen where anonymous thoughts float upward
 * like glowing embers. Users scroll through the stream, catch
 * drifts that resonate with them, or release their own.
 *
 * @param onDriftCaught Callback invoked with the drift ID when a drift is caught.
 * @param onNavigateToSky Callback to navigate to the Sky screen.
 * @param onNavigateToSettings Callback to navigate to the Settings screen.
 * @param viewModel The [OceanViewModel] for managing UI state and events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OceanScreen(
    onDriftCaught: (String) -> Unit, // Navigate to pocket with drift ID
    onNavigateToSky: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: OceanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var driftText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "drift",
                        fontWeight = FontWeight.Thin,
                        fontSize = 28.sp,
                        letterSpacing = 8.sp,
                        color = Color(0xFF00E676)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    TextButton(onClick = onNavigateToSky) {
                        Text("✦ sky", color = Color(0xFF00E676).copy(alpha = 0.7f))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(OceanEvent.ToggleReleaseDriftSheet) },
                containerColor = Color(0xFF00E676),
                contentColor = Color(0xFF0A0A0A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Release a drift")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // The floating drift feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                reverseLayout = false
            ) {
                items(state.drifts, key = { it.id }) { drift ->
                    DriftBubble(
                        drift = drift,
                        onCatch = { onDriftCaught(it.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            // Subtle gradient overlay at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0A0A0A), Color.Transparent)
                        )
                    )
            )
        }
    }

    // Bottom sheet to release a new drift
    if (state.showReleaseDriftSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(OceanEvent.ToggleReleaseDriftSheet) },
            containerColor = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "release a thought",
                    color = Color(0xFF00E676),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = driftText,
                    onValueChange = { driftText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    placeholder = {
                        Text(
                            "what's on your mind?",
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFE0E0E0),
                        unfocusedTextColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        viewModel.onEvent(OceanEvent.ReleaseDrift(driftText))
                        driftText = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color(0xFF0A0A0A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("let it drift", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
