package com.example.anonymouschat.feature_sky.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anonymouschat.core.model.Constellation

/**
 * The Sky — a personal screen displaying the user's active Constellations.
 *
 * Each Constellation appears as a gently pulsing star. Tapping a star
 * sends a signal to the other person. If they respond, a final Pocket
 * is created. After that meeting, the star fades forever.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyScreen(
    onNavigateBack: () -> Unit,
    onConstellationTapped: (String) -> Unit
) {
    // Mock constellations for now
    val constellations = remember {
        listOf(
            Constellation(id = "1", partnerDisplayName = "Silent Glow", partnerToken = "tok1"),
            Constellation(id = "2", partnerDisplayName = "Wandering Whisper", partnerToken = "tok2")
        )
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "✦ sky",
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
                            contentDescription = "Back to ocean",
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
        if (constellations.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✦",
                        fontSize = 48.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "your sky is empty",
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "constellations form when two\nsouls choose to remember each other",
                        color = Color(0xFF444444),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(constellations, key = { it.id }) { constellation ->
                    ConstellationCard(
                        constellation = constellation,
                        onTap = { onConstellationTapped(constellation.id) }
                    )
                }
            }
        }
    }
}

/**
 * A single Constellation card — a pulsing star representing
 * a one-time reconnection beacon with a past conversation partner.
 */
@Composable
private fun ConstellationCard(
    constellation: Constellation,
    onTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_pulse")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_scale"
    )
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing star icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(starScale)
                    .alpha(starAlpha)
                    .background(Color(0xFF00E676).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    fontSize = 24.sp,
                    color = Color(0xFF00E676)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = constellation.partnerDisplayName,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${constellation.usesRemaining} signal remaining",
                    color = Color(0xFF00E676).copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
