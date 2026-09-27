package com.example.anonymouschat.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val createdAt: Long = 0L,
    val lastSeen: Long = 0L,
    val fcmToken: String = ""
)
