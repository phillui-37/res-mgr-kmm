package xyz.kgy_production.res_mgr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.kgy_production.res_mgr.config.AppConfig

data class SettingsUiState(
    val serverUrl: String = "",
    val clientName: String = "",
    val isSaved: Boolean = false
)

class SettingsViewModel(
    private val config: AppConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(
        serverUrl = config.serverUrl,
        clientName = config.clientName
    ))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateServerUrl(url: String) {
        _uiState.update { it.copy(serverUrl = url, isSaved = false) }
    }

    fun updateClientName(name: String) {
        _uiState.update { it.copy(clientName = name, isSaved = false) }
    }

    fun saveSettings() {
        config.serverUrl = _uiState.value.serverUrl
        config.clientName = _uiState.value.clientName
        config.save()
        _uiState.update { it.copy(isSaved = true) }
    }
}

