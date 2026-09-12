package `in`.platesight.noinsta.ui

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Pairing : Screen("pairing")
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
}
