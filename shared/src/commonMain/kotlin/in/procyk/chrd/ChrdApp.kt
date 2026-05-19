package `in`.procyk.chrd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import `in`.procyk.chrd.db.AppSettings
import `in`.procyk.chrd.db.rememberAppSettingsRepository
import `in`.procyk.chrd.screen.Screen
import `in`.procyk.chrd.screen.SearchScreen
import `in`.procyk.chrd.screen.SongScreen
import `in`.procyk.chrd.viewmodel.SearchViewModel
import `in`.procyk.chrd.viewmodel.SongViewModel

@Composable
fun ChrdApp() {
    val settingsRepository = rememberAppSettingsRepository()
    val savedSettings by settingsRepository.settings.collectAsState(initial = AppSettings())
    ChrdTheme(themeMode = savedSettings.themeMode) {

        val backStack = rememberNavBackStack(Screen.SavedStateConfiguration, Screen.Search)

        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            entryProvider = entryProvider {
                entry<Screen.Search> {
                    SearchScreen(
                        viewModel = viewModel { SearchViewModel() },
                        settingsRepository = settingsRepository,
                        onSongSelected = { listing ->
                            backStack.add(
                                Screen.SongDetails(listing),
                            )
                        },
                    )
                }

                entry<Screen.SongDetails> { destination ->
                    SongScreen(
                        viewModel = viewModel(key = destination.listing.source.toString()) {
                            SongViewModel(destination.listing)
                        },
                    )
                }
            },
        )
    }
}
