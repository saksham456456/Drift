package com.example.anonymouschat.core.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a user's shifting anonymous identity.
 *
 * The identity evolves over time based on interaction quality:
 * - [warmth] ranges from 0f (cold/toxic → blue tones) to 1f (warm/kind → gold tones).
 * - [displayName] shifts based on warmth (e.g., "Cold Ash" → "Pale Ember" → "Golden Flame").
 * - [karmaScore] is the hidden metric that drives warmth changes.
 */
data class AnonymousIdentity(
    val uid: String = "",
    val displayName: String = "Pale Ember",
    val warmth: Float = 0.5f, // 0 = cold blue, 1 = warm gold
    val karmaScore: Int = 0
) {
    /**
     * Returns the avatar color on the cold-to-warm spectrum.
     * Cold (0f) = icy blue, Neutral (0.5f) = soft gray, Warm (1f) = golden amber.
     */
    val avatarColor: Color
        get() {
            val r = (warmth * 255).toInt().coerceIn(0, 255)
            val g = ((0.6f + warmth * 0.4f) * 180).toInt().coerceIn(0, 255)
            val b = ((1f - warmth) * 255).toInt().coerceIn(0, 255)
            return Color(r, g, b)
        }
}
