package com.example.anonymouschat.feature_ocean.presentation

import androidx.lifecycle.ViewModel
import com.example.anonymouschat.core.model.Drift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

/**
 * Holds the state for the Ocean screen — the main drift feed.
 *
 * @property drifts The list of drifts currently floating in the ocean.
 * @property isLoading Indicates whether drifts are currently being loaded.
 * @property showReleaseDriftSheet Controls the visibility of the drift release bottom sheet.
 */
data class OceanUiState(
    val drifts: List<Drift> = emptyList(),
    val isLoading: Boolean = false,
    val showReleaseDriftSheet: Boolean = false
)

/**
 * Events that the Ocean screen can fire.
 */
sealed interface OceanEvent {
    /**
     * Triggered when a user catches a drift.
     *
     * @property drift The [Drift] that was caught.
     */
    data class CatchDrift(val drift: Drift) : OceanEvent

    /**
     * Triggered when a user releases a new drift.
     *
     * @property text The text content of the drift.
     */
    data class ReleaseDrift(val text: String) : OceanEvent

    /**
     * Triggered to toggle the visibility of the release drift bottom sheet.
     */
    object ToggleReleaseDriftSheet : OceanEvent
}

/**
 * ViewModel for the Ocean screen managing drifts state and user actions.
 */
@HiltViewModel
class OceanViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OceanUiState())
    val uiState: StateFlow<OceanUiState> = _uiState.asStateFlow()

    init {
        // Load mock drifts for now (will be replaced with Firebase later)
        loadMockDrifts()
    }

    /** Processes UI events following Unidirectional Data Flow. */
    fun onEvent(event: OceanEvent) {
        when (event) {
            is OceanEvent.CatchDrift -> handleCatchDrift(event.drift)
            is OceanEvent.ReleaseDrift -> handleReleaseDrift(event.text)
            is OceanEvent.ToggleReleaseDriftSheet -> toggleSheet()
        }
    }

    private fun handleCatchDrift(drift: Drift) {
        // TODO: Create a Pocket and navigate to PocketScreen
    }

    private fun handleReleaseDrift(text: String) {
        if (text.isBlank()) return
        val newDrift = Drift(
            id = UUID.randomUUID().toString(),
            text = text.trim(),
            authorId = "local_user", // Will use Firebase UID later
            timestamp = System.currentTimeMillis()
        )
        _uiState.update { state ->
            state.copy(
                drifts = listOf(newDrift) + state.drifts,
                showReleaseDriftSheet = false
            )
        }
    }

    private fun toggleSheet() {
        _uiState.update { it.copy(showReleaseDriftSheet = !it.showReleaseDriftSheet) }
    }

    private fun loadMockDrifts() {
        val mocks = listOf(
            Drift(id = "1", text = "Does anyone else feel like they're just pretending to have it together?", authorId = "a1"),
            Drift(id = "2", text = "The 3am sky hits different when you have no one to text.", authorId = "a2"),
            Drift(id = "3", text = "I quit my job today. No backup plan. Terrified but alive.", authorId = "a3"),
            Drift(id = "4", text = "Sometimes I write letters to people I'll never send them to.", authorId = "a4"),
            Drift(id = "5", text = "What if we're all just strangers pretending we aren't lonely?", authorId = "a5"),
            Drift(id = "6", text = "I heard a song today that made me miss someone who doesn't exist yet.", authorId = "a6"),
            Drift(id = "7", text = "Is it weird that I feel more honest talking to strangers than to my best friend?", authorId = "a7"),
            Drift(id = "8", text = "I've been sitting in my car for 20 minutes. I just don't want to go inside yet.", authorId = "a8")
        )
        _uiState.update { it.copy(drifts = mocks) }
    }
}
