package `in`.platesight.noinsta.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.domain.model.AppEventType
import `in`.platesight.noinsta.domain.usecase.ProcessAppEventUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val securePreferencesManager: SecurePreferencesManager,
    private val processAppEventUseCase: ProcessAppEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            deviceName = android.os.Build.MODEL
        )
    )
    // Actually, I'll just use the deviceId for now if available.
    
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            deviceName = android.os.Build.MODEL,
            deviceId = securePreferencesManager.deviceId ?: "Unknown"
        )
    }

    fun triggerTestEvent() {
        viewModelScope.launch {
            processAppEventUseCase(AppEventType.INSTAGRAM_OPEN, "test_session_${UUID.randomUUID()}")
        }
    }
}

data class DashboardUiState(
    val deviceName: String = "",
    val deviceId: String = "",
    val stats: String = "No stats available yet"
)
