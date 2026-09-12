package `in`.platesight.noinsta.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.platesight.noinsta.data.SecurePreferencesManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val securePreferencesManager: SecurePreferencesManager
) : ViewModel() {

    val startDestination: String
        get() = when {
            !securePreferencesManager.isOnboarded -> Screen.Onboarding.route
            securePreferencesManager.accessToken == null -> Screen.Pairing.route
            else -> Screen.Dashboard.route
        }
}
