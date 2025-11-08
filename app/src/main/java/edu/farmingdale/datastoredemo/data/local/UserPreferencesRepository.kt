package edu.farmingdale.datastoredemo.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/*
 * Concrete implementation for accessing DataStore preferences
 *
 * IS_DARK_THEME saves the user's selected theme mode
 */
class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val IS_LINEAR_LAYOUT = booleanPreferencesKey("is_linear_layout")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        const val TAG = "UserPreferencesRepo"
    }

    // Emits the saved layout choice
    val isLinearLayout: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Log.e(TAG, "Error reading preferences.", e)
                emit(emptyPreferences())
            } else throw e
        }
        .map { it[IS_LINEAR_LAYOUT] ?: true }

    // Emits the saved theme choice
    val isDarkTheme: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Log.e(TAG, "Error reading preferences.", e)
                emit(emptyPreferences())
            } else throw e
        }
        .map { it[IS_DARK_THEME] ?: false }

    // Persist layout
    suspend fun saveLayoutPreference(isLinearLayout: Boolean) {
        dataStore.edit { prefs -> prefs[IS_LINEAR_LAYOUT] = isLinearLayout }
    }

    // Persist theme
    suspend fun saveThemePreference(isDarkTheme: Boolean) {
        dataStore.edit { prefs -> prefs[IS_DARK_THEME] = isDarkTheme }
    }
}
