package com.wavicompanion.app.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object WhatsApp : Screen("whatsapp")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
