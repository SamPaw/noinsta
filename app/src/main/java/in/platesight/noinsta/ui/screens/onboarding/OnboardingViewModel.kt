package `in`.platesight.noinsta.ui.screens.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.platesight.noinsta.data.SecurePreferencesManager
import `in`.platesight.noinsta.service.NoInstaForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    application: Application,
    private val securePreferencesManager: SecurePreferencesManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onOnboardingComplete() {
        securePreferencesManager.isOnboarded = true
        NoInstaForegroundService.start(getApplication())
    }
}

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isAccessibilityEnabled: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val isNotificationsEnabled: Boolean = false
)
