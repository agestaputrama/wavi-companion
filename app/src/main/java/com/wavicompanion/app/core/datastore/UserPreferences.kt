package com.wavicompanion.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wavicompanion.app.core.designsystem.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val HAS_ACTIVE_SESSION = booleanPreferencesKey("has_active_session")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { name ->
            try { ThemeMode.valueOf(name) } catch (_: IllegalArgumentException) { ThemeMode.SYSTEM }
        } ?: ThemeMode.SYSTEM
    }

    val keepScreenAwake: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.KEEP_SCREEN_AWAKE] ?: false
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val hasActiveSession: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.HAS_ACTIVE_SESSION] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        dataStore.edit { it[Keys.KEEP_SCREEN_AWAKE] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setHasActiveSession(active: Boolean) {
        dataStore.edit { it[Keys.HAS_ACTIVE_SESSION] = active }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
