package com.example.anonymouschat.data.model

data class Chat(
    val id: String = "",
    val type: String = "direct",  // "direct" or "group"
    val name: String = "",        // group name, empty for direct
    val participants: Map<String, Long> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val createdBy: String = ""
)
