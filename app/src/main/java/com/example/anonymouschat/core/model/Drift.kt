package com.example.anonymouschat.core.model

/**
 * Represents an anonymous thought released into the Ocean.
 * Drifts float through the feed for other users to discover and catch.
 */
data class Drift(
    val id: String = "",
    val text: String = "",
    val authorId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val catchCount: Int = 0
)
