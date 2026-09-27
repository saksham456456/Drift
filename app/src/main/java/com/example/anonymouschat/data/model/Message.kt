package com.example.anonymouschat.data.model

data class Message(
    val id: String = "",
    val text: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val timestamp: Long = 0L,
    val readBy: Map<String, Boolean> = emptyMap()
)
