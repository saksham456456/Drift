package com.example.anonymouschat.core.ui

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anonymouschat.core.model.Drift

/**
 * A single Drift bubble that visually represents an anonymous thought.
 *
 * Renders as a translucent, softly-glowing card with the drift text.
 * Supports long-press to "catch" the drift.
 *
 * @param drift The [Drift] data to display.
 * @param onCatch Called when the user long-presses to catch this drift.
 * @param modifier Standard Compose modifier.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DriftBubble(
    drift: Drift,
    onCatch: (Drift) -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "drift_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .alpha(alpha)
            .combinedClickable(
                onClick = { /* Single tap does nothing — drifts are observed */ },
                onLongClick = { onCatch(drift) }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2A1A) // Very dark green-tinted surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = drift.text,
                color = Color(0xFFE0E0E0),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "long press to catch",
                color = Color(0xFF4CAF50).copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}
