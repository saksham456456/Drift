package com.example.anonymouschat.core.ui

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * The Oxygen Ring — a visual heartbeat for Pocket conversations.
 *
 * Renders as a circular arc that depletes over time.
 * When conversation is active, the ring pulses gently.
 * When it fully depletes, the Pocket dissolves.
 *
 * @param progress Current oxygen level from 0f (empty) to 1f (full).
 * @param modifier Standard Compose modifier.
 */
@Composable
fun OxygenRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Gentle pulsing animation to simulate "breathing"
    val infiniteTransition = rememberInfiniteTransition(label = "oxygen_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "oxygen_alpha"
    )

    // Color shifts from green → yellow → red as oxygen depletes
    val ringColor = when {
        progress > 0.5f -> Color(0xFF00E676) // Neon green — healthy
        progress > 0.25f -> Color(0xFFFFEB3B) // Yellow — warning
        else -> Color(0xFFFF5252) // Red — critical
    }

    Canvas(
        modifier = modifier.size(48.dp)
    ) {
        val strokeWidth = 4.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        // Background track
        drawArc(
            color = Color(0xFF333333),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Active oxygen arc
        drawArc(
            color = ringColor.copy(alpha = pulseAlpha),
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
