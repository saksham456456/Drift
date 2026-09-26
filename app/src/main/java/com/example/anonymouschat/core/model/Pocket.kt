package com.example.anonymouschat.core.model

/**
 * A Pocket is a private, ephemeral conversation space created
 * when a user catches another user's Drift.
 * It has a finite oxygen supply — when the conversation dies,
 * the Pocket dissolves and all messages are permanently erased.
 */
data class Pocket(
    val id: String = "",
    val originDriftId: String = "",
    val originDriftText: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val oxygenDurationMs: Long = 15 * 60 * 1000L, // Default 15 minutes
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
