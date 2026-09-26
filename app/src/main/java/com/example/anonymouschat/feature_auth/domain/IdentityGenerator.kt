package com.example.anonymouschat.feature_auth.domain

import com.example.anonymouschat.core.model.AnonymousIdentity
import java.util.UUID

/**
 * Generates and evolves anonymous identities for the Drift ecosystem.
 *
 * Identities are not static — they shift based on a user's warmth score,
 * which is derived from their karma (how positively others interact with them).
 *
 * The naming follows a temperature metaphor:
 * - Cold names (low warmth): "Frozen Void", "Cold Ash", "Dim Spark"
 * - Neutral names (mid warmth): "Pale Ember", "Silent Glow", "Dusk Walker"
 * - Warm names (high warmth): "Bright Ember", "Golden Flame", "Radiant Star"
 */
object IdentityGenerator {

    private val coldAdjectives = listOf("Frozen", "Cold", "Dim", "Fading", "Hollow", "Pale", "Silent", "Distant")
    private val neutralAdjectives = listOf("Drifting", "Wandering", "Quiet", "Hidden", "Veiled", "Unnamed", "Passing")
    private val warmAdjectives = listOf("Bright", "Golden", "Radiant", "Glowing", "Luminous", "Vivid", "Blazing")

    private val coldNouns = listOf("Ash", "Void", "Shadow", "Mist", "Frost", "Echo", "Haze")
    private val neutralNouns = listOf("Ember", "Spark", "Glow", "Walker", "Whisper", "Drifter", "Wanderer")
    private val warmNouns = listOf("Flame", "Star", "Sun", "Phoenix", "Dawn", "Blaze", "Nova")

    /**
     * Creates a brand new identity with neutral warmth.
     * Called once when a user first opens the app.
     */
    fun generateFreshIdentity(): AnonymousIdentity {
        val name = "${neutralAdjectives.random()} ${neutralNouns.random()}"
        return AnonymousIdentity(
            uid = UUID.randomUUID().toString(),
            displayName = name,
            warmth = 0.5f,
            karmaScore = 0
        )
    }

    /**
     * Evolves an existing identity based on its current karma score.
     * This is called periodically as the user interacts with the app.
     *
     * @param identity The current identity to evolve.
     * @return A new [AnonymousIdentity] with an updated name and warmth.
     */
    fun evolveIdentity(identity: AnonymousIdentity): AnonymousIdentity {
        val newWarmth = karmaToWarmth(identity.karmaScore)
        val newName = generateNameForWarmth(newWarmth)
        return identity.copy(
            displayName = newName,
            warmth = newWarmth
        )
    }

    /**
     * Maps a karma score to a warmth value between 0f and 1f.
     * Uses a sigmoid-like curve so extreme karma has diminishing returns.
     */
    private fun karmaToWarmth(karma: Int): Float {
        // Sigmoid mapping: karma of -100 → ~0.05, 0 → 0.5, +100 → ~0.95
        return (1.0f / (1.0f + Math.exp(-karma / 30.0).toFloat()))
    }

    /**
     * Selects an appropriate adjective-noun pair based on warmth level.
     */
    private fun generateNameForWarmth(warmth: Float): String {
        val (adjectives, nouns) = when {
            warmth < 0.33f -> coldAdjectives to coldNouns
            warmth < 0.66f -> neutralAdjectives to neutralNouns
            else -> warmAdjectives to warmNouns
        }
        return "${adjectives.random()} ${nouns.random()}"
    }
}
