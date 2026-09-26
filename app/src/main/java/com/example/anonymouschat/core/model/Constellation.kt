package com.example.anonymouschat.core.model

/**
 * A Constellation is a one-time-use reconnection beacon.
 *
 * When two users have a meaningful Pocket conversation,
 * they can both agree to form a Constellation — a single star
 * that lets them find each other exactly ONE more time.
 * After that second meeting, the Constellation fades forever.
 */
data class Constellation(
    val id: String = "",
    val partnerDisplayName: String = "",
    val partnerToken: String = "",
    val usesRemaining: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
