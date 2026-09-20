package `in`.platesight.noinsta.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.data.remote.ApiService
import `in`.platesight.noinsta.data.remote.model.DailyBreakdownDto
import `in`.platesight.noinsta.data.remote.model.EventLogItemDto
import `in`.platesight.noinsta.data.remote.model.InterventionLogItemDto
import `in`.platesight.noinsta.domain.model.AppEventType
import `in`.platesight.noinsta.domain.usecase.ProcessAppEventUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val securePreferencesManager: SecurePreferencesManager,
    private val processAppEventUseCase: ProcessAppEventUseCase,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            deviceName = android.os.Build.MODEL,
            deviceId = securePreferencesManager.deviceId ?: "Unknown"
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.getAnalytics()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            totalOpensToday = body.summary.totalOpensToday,
                            totalTimeTodaySeconds = body.summary.totalTimeTodaySeconds,
                            totalInterventionsToday = body.summary.totalInterventionsToday,
                            totalOpensAllTime = body.summary.totalOpensAllTime,
                            totalSessionsAllTime = body.summary.totalSessionsAllTime,
                            totalInterventionsAllTime = body.summary.totalInterventionsAllTime,
                            dailyBreakdown = body.dailyBreakdown,
                            recentEvents = body.recentEvents,
                            recentInterventions = body.recentInterventions
                        )
                    }
                } else {
                    val errorMsg = "Server error ${response.code()}"
                    Log.w("DashboardViewModel", "Failed to fetch analytics: $errorMsg")
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching analytics", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Network error") }
            }
        }
    }

    fun triggerTestEvent() {
        viewModelScope.launch {
            processAppEventUseCase(AppEventType.INSTAGRAM_OPEN, "test_session_${UUID.randomUUID()}")
            delay(1500)
            refreshAnalytics()
        }
    }
}

data class DashboardUiState(
    val deviceName: String = "",
    val deviceId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalOpensToday: Int = 0,
    val totalTimeTodaySeconds: Int = 0,
    val totalInterventionsToday: Int = 0,
    val totalOpensAllTime: Int = 0,
    val totalSessionsAllTime: Int = 0,
    val totalInterventionsAllTime: Int = 0,
    val dailyBreakdown: List<DailyBreakdownDto> = emptyList(),
    val recentEvents: List<EventLogItemDto> = emptyList(),
    val recentInterventions: List<InterventionLogItemDto> = emptyList()
)
