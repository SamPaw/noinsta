package `in`.platesight.noinsta.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.ApiService
import `in`.platesight.noinsta.data.remote.model.UpdateUserSettingsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val securePreferencesManager: SecurePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            cooldownSeconds = securePreferencesManager.cooldownSeconds
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val response = apiService.getSettings()
                if (response.isSuccessful && response.body() != null) {
                    val remoteCooldown = response.body()!!.cooldownSeconds
                    securePreferencesManager.cooldownSeconds = remoteCooldown
                    _uiState.update { it.copy(cooldownSeconds = remoteCooldown) }
                }
            } catch (e: Exception) {
                Log.w("SettingsViewModel", "Failed to load remote settings: ${e.message}")
            }
        }
    }

    fun updateCooldown(seconds: Int) {
        viewModelScope.launch {
            securePreferencesManager.cooldownSeconds = seconds
            _uiState.update { it.copy(cooldownSeconds = seconds, isSavingCooldown = true) }
            try {
                val response = apiService.updateSettings(UpdateUserSettingsRequest(cooldownSeconds = seconds))
                if (response.isSuccessful && response.body() != null) {
                    val saved = response.body()!!.cooldownSeconds
                    _uiState.update {
                        it.copy(
                            cooldownSeconds = saved,
                            isSavingCooldown = false,
                            statusMessage = "Cooldown updated to ${formatCooldownLabel(saved)}"
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSavingCooldown = false, statusMessage = "Updated locally (offline)") }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error saving cooldown", e)
                _uiState.update { it.copy(isSavingCooldown = false, statusMessage = "Updated locally (sync pending)") }
            }
        }
    }

    fun checkHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingHealth = true) }
            val startTime = System.currentTimeMillis()
            try {
                val response = apiService.checkHealth()
                val latency = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isCheckingHealth = false,
                            healthStatus = "Healthy (${latency}ms)"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isCheckingHealth = false,
                            healthStatus = "Error: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCheckingHealth = false,
                        healthStatus = "Failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun unpair(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                apiService.revokeToken()
            } catch (e: Exception) {
                // Ignore errors on revoke
            }
            securePreferencesManager.clear()
            onComplete()
        }
    }

    private fun formatCooldownLabel(seconds: Int): String {
        return when (seconds) {
            0 -> "Disabled (0 min)"
            else -> "${seconds / 60} min"
        }
    }
}

data class SettingsUiState(
    val healthStatus: String = "Unknown",
    val isCheckingHealth: Boolean = false,
    val cooldownSeconds: Int = 300,
    val isSavingCooldown: Boolean = false,
    val statusMessage: String? = null
)
