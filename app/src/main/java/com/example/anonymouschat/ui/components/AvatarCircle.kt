package com.example.anonymouschat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

val AvatarColors = listOf(
    Color(0xFFE57373),
    Color(0xFFF06292),
    Color(0xFFBA68C8),
    Color(0xFF9575CD),
    Color(0xFF7986CB),
    Color(0xFF64B5F6),
    Color(0xFF4FC3F7),
    Color(0xFF4DD0E1),
    Color(0xFF4DB6AC),
    Color(0xFF81C784),
    Color(0xFFAED581),
    Color(0xFFFF8A65),
    Color(0xFFD4E157),
    Color(0xFFFFD54F),
    Color(0xFFFFB74D),
    Color(0xFFA1887F)
)

@Composable
fun AvatarCircle(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val initial = remember(displayName) {
        if (displayName.isNotBlank()) displayName.first().uppercase() else "?"
    }
    
    val color = remember(displayName) {
        val hash = displayName.hashCode().absoluteValue
        AvatarColors[hash % AvatarColors.size]
    }

    Box(
        modifier = modifier
            .size(size)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.Medium
        )
    }
}
