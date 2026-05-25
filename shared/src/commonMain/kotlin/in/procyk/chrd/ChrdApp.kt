package `in`.procyk.chrd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
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
import `in`.procyk.chrd.screen.*
import `in`.procyk.chrd.viewmodel.SearchViewModel
import `in`.procyk.chrd.viewmodel.SongViewModel

@Composable
fun ChrdApp() {
    val settingsRepository = rememberAppSettingsRepository()
    val savedSettings by settingsRepository.settings.collectAsState(initial = AppSettings())
    ChrdTheme(themeMode = savedSettings.themeMode) {
        val backStack = rememberNavBackStack(Screen.SavedStateConfiguration, Screen.Search)

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val currentScreen = backStack.last()
                    NavigationBarItem(
                        selected = currentScreen is Screen.Search || currentScreen is Screen.SongDetails,
                        onClick = {
                            if (currentScreen !is Screen.Search && currentScreen !is Screen.SongDetails) {
                                backStack.clear()
                                backStack.add(Screen.Search)
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Songs") },
                        label = { Text("Songs") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Favorites,
                        onClick = {
                            if (currentScreen !is Screen.Favorites) {
                                backStack.clear()
                                backStack.add(Screen.Favorites)
                            }
                        },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Settings,
                        onClick = {
                            if (currentScreen !is Screen.Settings) {
                                backStack.clear()
                                backStack.add(Screen.Settings)
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { padding ->
            NavDisplay(
                modifier = Modifier.padding(padding).fillMaxSize(),
                backStack = backStack,
                entryProvider = entryProvider {
                    entry<Screen.Search> {
                        SearchScreen(
                            viewModel = viewModel { SearchViewModel() },
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

                    entry<Screen.Favorites> {
                        FavoritesScreen()
                    }

                    entry<Screen.Settings> {
                        SettingsScreen(
                            settingsRepository = settingsRepository,
                        )
                    }
                },
            )
        }
    }
}
