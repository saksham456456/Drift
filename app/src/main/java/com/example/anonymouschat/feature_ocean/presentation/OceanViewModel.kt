package com.example.anonymouschat.feature_ocean.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anonymouschat.core.model.Drift
import com.example.anonymouschat.data.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class OceanViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OceanUiState())
    val uiState: StateFlow<OceanUiState> = _uiState.asStateFlow()

    init {
        // Sign in anonymously and subscribe to live Drifts
        viewModelScope.launch {
            repository.signInAnonymously()
            repository.getDrifts().collect { liveDrifts ->
                _uiState.update { it.copy(drifts = liveDrifts) }
            }
        }
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
        // Handled via UI navigation, but could increment catch count here later
    }

    private fun handleReleaseDrift(text: String) {
        if (text.isBlank()) return
        val newDrift = Drift(
            id = UUID.randomUUID().toString(),
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        
        // Optimistically hide the sheet
        _uiState.update { it.copy(showReleaseDriftSheet = false) }
        
        viewModelScope.launch {
            repository.releaseDrift(newDrift)
        }
    }

    private fun toggleSheet() {
        _uiState.update { it.copy(showReleaseDriftSheet = !it.showReleaseDriftSheet) }
    }
}
