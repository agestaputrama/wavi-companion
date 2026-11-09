package com.wavicompanion.app.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {

    @Test
    fun `all screen routes are unique`() {
        val routes = listOf(
            Screen.Splash.route,
            Screen.Onboarding.route,
            Screen.Dashboard.route,
            Screen.WhatsApp.route,
            Screen.Settings.route,
            Screen.About.route,
        )
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun `screen routes are non-empty lowercase strings`() {
        val screens = listOf(
            Screen.Splash,
            Screen.Onboarding,
            Screen.Dashboard,
            Screen.WhatsApp,
            Screen.Settings,
            Screen.About,
        )
        screens.forEach { screen ->
            assert(screen.route.isNotBlank())
            assertEquals(screen.route, screen.route.lowercase())
        }
    }
}
