package com.example.anonymouschat.feature_auth.domain

object IdentityGenerator {
    private val adjectives = listOf("Brave", "Silent", "Neon", "Crimson", "Shadow", "Wandering", "Cosmic", "Lunar", "Solar", "Hidden")
    private val nouns = listOf("Fox", "Wolf", "Raven", "Owl", "Panther", "Viper", "Hawk", "Bear", "Lynx", "Ghost")

    fun generateName(): String {
        return "${adjectives.random()} ${nouns.random()}"
    }
}
