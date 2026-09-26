package com.example.anonymouschat.feature_pocket.presentation
import androidx.lifecycle.ViewModelProvider
import com.example.anonymouschat.data.DefaultDataRepository

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anonymouschat.core.model.Message
import com.example.anonymouschat.data.DataRepository

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


/**
 * UI state for a Pocket conversation.
 */
data class PocketUiState(
    val messages: List<Message> = emptyList(),
    val oxygenProgress: Float = 1f, // 1 = full, 0 = dissolved
    val isDissolved: Boolean = false,
    val originDriftText: String = "",
    val showConstellationPrompt: Boolean = false
)

/** Events fired from the Pocket screen. */
sealed interface PocketEvent {
    data class SendMessage(val text: String) : PocketEvent
    data object FormConstellation : PocketEvent
    data object DeclineConstellation : PocketEvent
}

/**
 * ViewModel managing the lifecycle and state of a Pocket conversation.
 */


class PocketViewModel (
    private val driftId: String,
    private val repository: DataRepository = DefaultDataRepository()
) : ViewModel() {

    class Factory(private val driftId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PocketViewModel(driftId) as T
        }
    }

    

    private val _uiState = MutableStateFlow(PocketUiState())
    val uiState: StateFlow<PocketUiState> = _uiState.asStateFlow()

    // Total oxygen duration in milliseconds (user-configurable, default 15 min)
    private val oxygenTotalMs = 15 * 60 * 1000L
    private var oxygenRemainingMs = oxygenTotalMs
    private var oxygenJob: Job? = null

    init {
        // Load the origin drift text (mock for now)
        _uiState.update { it.copy(originDriftText = "A thought you caught from the ocean...") }
        startOxygenCountdown()
        
        // Listen to live messages
        viewModelScope.launch {
            repository.getPocketMessages(driftId).collect { liveMsgs ->
                _uiState.update { it.copy(messages = liveMsgs) }
                refreshOxygen() // Reset oxygen on any new message
            }
        }
    }

    /** Processes UI events following Unidirectional Data Flow. */
    fun onEvent(event: PocketEvent) {
        when (event) {
            is PocketEvent.SendMessage -> handleSendMessage(event.text)
            is PocketEvent.FormConstellation -> handleFormConstellation()
            is PocketEvent.DeclineConstellation -> handleDeclineConstellation()
        }
    }

    private fun handleSendMessage(text: String) {
        if (text.isBlank() || _uiState.value.isDissolved) return

        val message = Message(
            id = UUID.randomUUID().toString(),
            pocketId = driftId,
            text = text.trim()
        )

        viewModelScope.launch {
            repository.sendMessage(message)
        }

        // Refresh oxygen — activity extends the conversation
        refreshOxygen()
    }

    /**
     * Starts the oxygen countdown. The ring depletes over time.
     * Each message sent resets the inactivity timer.
     */
    private fun startOxygenCountdown() {
        oxygenJob?.cancel()
        oxygenJob = viewModelScope.launch {
            val tickInterval = 1000L // Update every second
            while (oxygenRemainingMs > 0) {
                delay(tickInterval)
                oxygenRemainingMs -= tickInterval
                val progress = (oxygenRemainingMs.toFloat() / oxygenTotalMs).coerceIn(0f, 1f)
                _uiState.update { it.copy(oxygenProgress = progress) }
            }
            // Oxygen depleted — dissolve the pocket
            _uiState.update { it.copy(isDissolved = true, showConstellationPrompt = true) }
        }
    }

    /** Resets oxygen to full when user sends a message (activity detected). */
    private fun refreshOxygen() {
        oxygenRemainingMs = oxygenTotalMs
        _uiState.update { it.copy(oxygenProgress = 1f) }
    }

    private fun handleFormConstellation() {
        // TODO: Create constellation beacon
        _uiState.update { it.copy(showConstellationPrompt = false) }
    }

    private fun handleDeclineConstellation() {
        _uiState.update { it.copy(showConstellationPrompt = false) }
    }
}

