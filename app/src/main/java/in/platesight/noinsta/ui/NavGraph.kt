package `in`.platesight.noinsta.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NoInstaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            // Placeholder
        }
        composable(Screen.Pairing.route) {
            // Placeholder
        }
        composable(Screen.Dashboard.route) {
            // Placeholder
        }
        composable(Screen.Settings.route) {
            // Placeholder
        }
    }
}
