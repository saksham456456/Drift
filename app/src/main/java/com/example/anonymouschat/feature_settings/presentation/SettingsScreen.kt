package com.example.anonymouschat.feature_settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Settings screen where the user has full control over their Drift experience.
 *
 * All privacy and behavior settings are in the user's hands:
 * - Oxygen duration (how long Pockets stay alive)
 * - Identity reset (regenerate their anonymous name)
 * - Message expiry preferences
 * - About section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    // Local state for settings (will be backed by DataStore later)
    var oxygenMinutes by remember { mutableFloatStateOf(15f) }
    var screenshotWarning by remember { mutableStateOf(true) }
    var showIdentityResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "settings",
                        fontWeight = FontWeight.Thin,
                        fontSize = 24.sp,
                        letterSpacing = 6.sp,
                        color = Color(0xFF00E676)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Pocket Settings ──
            SettingsSectionHeader(title = "pockets")

            Text(
                text = "oxygen duration",
                color = Color(0xFFE0E0E0),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "How long a pocket stays alive without activity",
                color = Color(0xFF666666),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Slider(
                    value = oxygenMinutes,
                    onValueChange = { oxygenMinutes = it },
                    valueRange = 5f..60f,
                    steps = 10,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E676),
                        activeTrackColor = Color(0xFF00E676),
                        inactiveTrackColor = Color(0xFF333333)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${oxygenMinutes.toInt()} min",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(
                color = Color(0xFF222222),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // ── Privacy Settings ──
            SettingsSectionHeader(title = "privacy")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "screenshot warning",
                        color = Color(0xFFE0E0E0),
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Alert you if someone screenshots a pocket",
                        color = Color(0xFF666666),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Switch(
                    checked = screenshotWarning,
                    onCheckedChange = { screenshotWarning = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color(0xFF666666),
                        uncheckedTrackColor = Color(0xFF333333)
                    )
                )
            }

            HorizontalDivider(
                color = Color(0xFF222222),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // ── Identity Settings ──
            SettingsSectionHeader(title = "identity")

            Button(
                onClick = { showIdentityResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color(0xFFFF5252)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("reset identity", fontWeight = FontWeight.Light, letterSpacing = 2.sp)
            }
            Text(
                text = "This will erase your current name, karma, and all constellations. You'll start completely fresh.",
                color = Color(0xFF666666),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            HorizontalDivider(
                color = Color(0xFF222222),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // ── About ──
            SettingsSectionHeader(title = "about")
            Text(
                text = "drift v1.0.0",
                color = Color(0xFF666666),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "thoughts float. you catch the ones that resonate.",
                color = Color(0xFF444444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )
        }
    }

    // Identity reset confirmation dialog
    if (showIdentityResetDialog) {
        AlertDialog(
            onDismissRequest = { showIdentityResetDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text(
                    "reset identity?",
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
            },
            text = {
                Text(
                    "Your name, warmth, karma, and all constellations will be permanently erased. This cannot be undone.",
                    color = Color(0xFFE0E0E0).copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // TODO: Reset identity via IdentityGenerator
                    showIdentityResetDialog = false
                }) {
                    Text("erase everything", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showIdentityResetDialog = false }) {
                    Text("keep my identity", color = Color(0xFF00E676))
                }
            }
        )
    }
}

/**
 * A minimal section header for the settings screen.
 */
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF00E676).copy(alpha = 0.7f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 4.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}
