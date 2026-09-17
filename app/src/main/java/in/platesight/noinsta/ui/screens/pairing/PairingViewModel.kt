package `in`.platesight.noinsta.ui.screens.pairing

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.ApiService
import `in`.platesight.noinsta.data.remote.model.PairingClaimRequest
import `in`.platesight.noinsta.service.NoInstaForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService,
    private val securePreferencesManager: SecurePreferencesManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PairingUiState(deviceName = Build.MODEL))
    val uiState = _uiState.asStateFlow()

    fun onPairingCodeChanged(code: String) {
        _uiState.value = _uiState.value.copy(pairingCode = code)
    }

    fun onDeviceNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(deviceName = name)
    }

    fun claimPairing(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.pairingCode.length != 6) return

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val request = PairingClaimRequest(
                    pairingCode = currentState.pairingCode,
                    deviceName = currentState.deviceName
                )
                val response = apiService.claimPairing(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    securePreferencesManager.deviceId = body.deviceId
                    securePreferencesManager.accessToken = body.accessToken
                    securePreferencesManager.refreshToken = body.refreshToken
                    securePreferencesManager.userId = body.userId
                    
                    NoInstaForegroundService.start(getApplication())

                    onSuccess()
                } else {
                    val errorMessage = when (response.code()) {
                        403 -> "Network firewall blocked request (HTTP 403). Please disconnect from Wi-Fi (e.g. campus Wi-Fi) and try on Mobile Data or VPN."
                        404 -> "Pairing code expired or invalid (HTTP 404)."
                        400 -> "Invalid pairing request (HTTP 400)."
                        else -> "Failed to claim pairing code: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error claiming pairing")
                val errorMessage = if (e.message?.contains("CertPathValidatorException") == true ||
                                       e.message?.contains("Trust anchor") == true) {
                    "SSL certificate blocked by network. Please use Mobile Data or a VPN."
                } else {
                    e.message ?: "Unknown error"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }
}

data class PairingUiState(
    val pairingCode: String = "",
    val deviceName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
