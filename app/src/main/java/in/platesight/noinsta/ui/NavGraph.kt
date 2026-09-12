package `in`.platesight.noinsta.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import `in`.platesight.noinsta.ui.screens.dashboard.DashboardScreen
import `in`.platesight.noinsta.ui.screens.onboarding.OnboardingScreen
import `in`.platesight.noinsta.ui.screens.pairing.PairingScreen
import `in`.platesight.noinsta.ui.screens.settings.SettingsScreen

@Composable
fun NoInstaNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = viewModel.startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Pairing.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Pairing.route) {
            PairingScreen(
                onPairingSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Pairing.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUnpair = {
                    navController.navigate(Screen.Pairing.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
