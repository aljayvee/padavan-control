package com.example.padavancontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.padavancontrol.data.PadavanRepository
import com.example.padavancontrol.data.models.LanClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.isActive

data class DevicesUiState(
    val clients: List<LanClient> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class DevicesViewModel(
    private val repository: PadavanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var isPollingActive = false
    private val pollingBackoff = PollingBackoff(baseDelayMs = 5000L)

    fun setPollingEnabled(enabled: Boolean) {
        if (enabled == isPollingActive) return
        isPollingActive = enabled
        if (enabled) {
            startPolling()
        } else {
            pollingJob?.cancel()
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                var success = false
                repository.getLanClients().collect { result ->
                    result.onSuccess { list ->
                        success = true
                        _uiState.update { it.copy(clients = list, isLoading = false, errorMessage = null) }
                    }
                    result.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to fetch clients") }
                    }
                }
                if (success) pollingBackoff.onSuccess() else pollingBackoff.onFailure()
                delay(pollingBackoff.nextDelayMs())
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleBlockStatus(client: LanClient) {
        viewModelScope.launch {
            val success = if (client.blockIndex != -1) {
                repository.unblockClient(client.blockIndex)
            } else {
                repository.blockClient(client.macAddress)
            }
            
            if (success) {
                // Poll immediately after a short delay to get the new state
                delay(2000)
                startPolling()
            }
        }
    }

    public override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
