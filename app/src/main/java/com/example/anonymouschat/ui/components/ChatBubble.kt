package com.example.anonymouschat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anonymouschat.theme.ChatBubbleOther
import com.example.anonymouschat.theme.ChatBubbleSelf
import com.example.anonymouschat.theme.TextSecondary
import com.example.anonymouschat.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun ChatBubble(
    message: String,
    senderName: String,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier,
    showSenderName: Boolean = false,
    timestamp: Long
) {
    val timeString = remember(timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    }
    
    val configuration = LocalConfiguration.current
    val maxWidth = remember(configuration) { (configuration.screenWidthDp * 0.75).dp }
    
    val nameColor = remember(senderName) {
        val hash = senderName.hashCode().absoluteValue
        AvatarColors[hash % AvatarColors.size]
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            if (showSenderName && !isOwnMessage) {
                Text(
                    text = senderName,
                    color = nameColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .background(
                        color = if (isOwnMessage) ChatBubbleSelf else ChatBubbleOther,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                            bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message,
                    color = if (isOwnMessage) White else Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            }
            
            Text(
                text = timeString,
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = if (isOwnMessage) 0.dp else 12.dp, end = if (isOwnMessage) 12.dp else 0.dp)
            )
        }
    }
}
