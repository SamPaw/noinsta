package `in`.platesight.noinsta.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val securePreferencesManager: SecurePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun checkHealth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingHealth = true)
            val startTime = System.currentTimeMillis()
            try {
                val response = apiService.checkHealth()
                val latency = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isCheckingHealth = false,
                        healthStatus = "Healthy (${latency}ms)"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCheckingHealth = false,
                        healthStatus = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingHealth = false,
                    healthStatus = "Failed: ${e.message}"
                )
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
}

data class SettingsUiState(
    val healthStatus: String = "Unknown",
    val isCheckingHealth: Boolean = false
)
