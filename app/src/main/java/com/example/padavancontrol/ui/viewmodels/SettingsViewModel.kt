package com.example.padavancontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.padavancontrol.data.CredentialStore
import com.example.padavancontrol.data.SettingsDataStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val routerIp: String = "",
    val routerName: String = "",
    val hardwareModel: String = "",
    val rememberMe: Boolean = false,
    val useBiometric: Boolean = false,
    val monitoringEnabled: Boolean = false,
    val monitorIntervalMinutes: Int = 15,
    val notifyWanDisconnect: Boolean = true,
    val notifyNewDevice: Boolean = true,
    val notifyHighTemp: Boolean = true,
    val highTempThreshold: Int = 75
)

class SettingsViewModel(
    private val credentialStore: CredentialStore,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var ipSaveJob: Job? = null

    init {
        _uiState.update {
            it.copy(
                routerIp = credentialStore.getRouterIp(),
                routerName = credentialStore.getRouterName(),
                hardwareModel = credentialStore.getHardwareModel(),
                rememberMe = credentialStore.isRememberCredentials(),
                useBiometric = credentialStore.isUseBiometric()
            )
        }

        viewModelScope.launch {
            settingsDataStore.getMonitoringPreferences().collect { prefs ->
                _uiState.update {
                    it.copy(
                        monitoringEnabled = prefs.monitoringEnabled,
                        monitorIntervalMinutes = prefs.monitorIntervalMinutes,
                        notifyWanDisconnect = prefs.notifyWanDisconnect,
                        notifyNewDevice = prefs.notifyNewDevice,
                        notifyHighTemp = prefs.notifyHighTemp,
                        highTempThreshold = prefs.highTempThreshold
                    )
                }
            }
        }
    }

    /**
     * The settings view model is created before login, so its initial router
     * address may still be the old/default one. Refresh it when the screen is
     * opened so Connection Details always reflects the router that was just
     * discovered and saved.
     */
    fun refreshConnectionInfo() {
        _uiState.update {
            it.copy(
                routerIp = credentialStore.getRouterIp(),
                routerName = credentialStore.getRouterName(),
                hardwareModel = credentialStore.getHardwareModel(),
                rememberMe = credentialStore.isRememberCredentials(),
                useBiometric = credentialStore.isUseBiometric()
            )
        }
    }

    fun updateRouterIp(value: String) {
        _uiState.update { it.copy(routerIp = value) }

        // Debounce: cancel any pending save, then schedule a new one after 500ms
        ipSaveJob?.cancel()
        ipSaveJob = viewModelScope.launch {
            delay(500)
            credentialStore.saveRouterIp(value)
        }
    }

    fun updateRememberMe(value: Boolean) {
        _uiState.update { it.copy(rememberMe = value) }
        credentialStore.saveCredentials(
            credentialStore.getUsername(),
            credentialStore.getRouterIp(),
            credentialStore.getPassword(),
            value,
            _uiState.value.useBiometric
        )
    }

    fun updateUseBiometric(value: Boolean) {
        _uiState.update { it.copy(useBiometric = value) }
        credentialStore.saveCredentials(
            credentialStore.getUsername(),
            credentialStore.getRouterIp(),
            credentialStore.getPassword(),
            _uiState.value.rememberMe,
            value
        )
    }

    fun updateMonitoringEnabled(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setMonitoringEnabled(value)
        }
    }

    fun updateMonitorIntervalMinutes(value: Int) {
        viewModelScope.launch {
            settingsDataStore.setMonitorIntervalMinutes(value)
        }
    }

    fun updateNotifyWanDisconnect(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotifyWanDisconnect(value)
        }
    }

    fun updateNotifyNewDevice(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotifyNewDevice(value)
        }
    }

    fun updateNotifyHighTemp(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotifyHighTemp(value)
        }
    }

    fun updateHighTempThreshold(value: Int) {
        viewModelScope.launch {
            settingsDataStore.setHighTempThreshold(value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // If there's a pending debounced IP save, persist it immediately
        val currentIp = _uiState.value.routerIp
        if (ipSaveJob?.isActive == true) {
            ipSaveJob?.cancel()
            credentialStore.saveRouterIp(currentIp)
        }
    }
}
