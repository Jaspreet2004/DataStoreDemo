package edu.farmingdale.datastoredemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.farmingdale.datastoredemo.R
import edu.farmingdale.datastoredemo.EmojiReleaseApplication
import edu.farmingdale.datastoredemo.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class EmojiScreenViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // UI states access for various

    // Combines both stored preferences into a single UI state
    val uiState: StateFlow<EmojiReleaseUiState> =
        userPreferencesRepository.isLinearLayout
            .combine(userPreferencesRepository.isDarkTheme) { isLinear, isDark ->
                EmojiReleaseUiState(isLinearLayout = isLinear, isDarkTheme = isDark)
        }.stateIn(
            scope = viewModelScope,
            // Flow is set to emits value for when app is on the foreground
            // 5 seconds stop delay is added to ensure it flows continuously
            // for cases such as configuration change
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EmojiReleaseUiState()
        )

     //  changes the layout and icons and save the selection

    fun selectLayout(isLinearLayout: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveLayoutPreference(isLinearLayout)
        }
    }

     // Toggle light and dark theme

    fun selectTheme(isDark: Boolean) {
        viewModelScope.launch { userPreferencesRepository.saveThemePreference(isDark) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as EmojiReleaseApplication)
                EmojiScreenViewModel(application.userPreferencesRepository)
            }
        }
    }
}

/*
 * Data class containing various UI States for Emoji Release screens
 * isLinearLayout checks if emojis are displayed in a list or grid view and controls which layout toggle icon and description are shown
 * isDarkTheme shows if the app is using the dark theme or not
 */
data class EmojiReleaseUiState(
    val isLinearLayout: Boolean = true,
    val isDarkTheme: Boolean = false,
    val toggleContentDescription: Int =
        if (isLinearLayout) R.string.grid_layout_toggle else R.string.linear_layout_toggle,
    val toggleIcon: Int =
        if (isLinearLayout) R.drawable.ic_grid_layout else R.drawable.ic_linear_layout
)
