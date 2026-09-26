package com.example.anonymouschat.core.model

/**
 * A single message within a Pocket conversation.
 * Messages are ephemeral — they are destroyed when the Pocket dissolves.
 */
data class Message(
    val id: String = "",
    val pocketId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
