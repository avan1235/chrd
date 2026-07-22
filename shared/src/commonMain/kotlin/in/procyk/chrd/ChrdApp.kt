package `in`.procyk.chrd

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import `in`.procyk.chrd.component.liquid.LiquidBottomTab
import `in`.procyk.chrd.component.liquid.LiquidBottomTabs
import `in`.procyk.chrd.db.AppSettings
import `in`.procyk.chrd.db.ThemeMode
import `in`.procyk.chrd.db.rememberAppSettingsRepository
import `in`.procyk.chrd.db.rememberSongRepository
import `in`.procyk.chrd.screen.*
import `in`.procyk.chrd.viewmodel.SearchViewModel
import `in`.procyk.chrd.viewmodel.SongViewModel

private enum class TabEntry(
    val icon: ImageVector,
    val label: String,
) {
    SONGS(Icons.Default.Search, "Songs"),
    FAVORITES(Icons.Default.Favorite, "Favorites"),
    SETTINGS(Icons.Default.Settings, "Settings"),
}

@Composable
fun ChrdApp() {
    val settingsRepository = rememberAppSettingsRepository()
    val songRepository = rememberSongRepository()
    val savedSettings by settingsRepository.settings.collectAsState(initial = AppSettings())
    ChrdTheme(themeMode = savedSettings.themeMode) {
        val backStack = rememberNavBackStack(Screen.SavedStateConfiguration, Screen.Search)

        var isBarsVisible by remember { mutableStateOf(true) }
        LaunchedEffect(backStack.last()) {
            isBarsVisible = true
        }

        Scaffold(
            bottomBar = {
                if (!savedSettings.useLiquidNavigation) {
                    AnimatedVisibility(
                        visible = isBarsVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
                    ) {
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
                                label = { Text("Songs") },
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
                                label = { Text("Favorites") },
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
                                label = { Text("Settings") },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            val backdrop = rememberLayerBackdrop()
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                NavDisplay(
                    modifier = Modifier.padding(padding)
                        .layerBackdrop(backdrop)
                        .fillMaxSize(),
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
                                useLiquidNavigation = savedSettings.useLiquidNavigation,
                            )
                        }

                        entry<Screen.SongDetails> { destination ->
                            SongScreen(
                                viewModel = viewModel(key = destination.listing.source.toString()) {
                                    SongViewModel(destination.listing, songRepository)
                                },
                                onAutoScrollingChanged = { isBarsVisible = !it },
                            )
                        }

                        entry<Screen.Favorites> {
                            FavoritesScreen(
                                repository = songRepository,
                                onSongSelected = { listing ->
                                    backStack.add(Screen.SongDetails(listing))
                                },
                                useLiquidNavigation = savedSettings.useLiquidNavigation,
                            )
                        }

                        entry<Screen.Settings> {
                            SettingsScreen(
                                settingsRepository = settingsRepository,
                                useLiquidNavigation = savedSettings.useLiquidNavigation,
                            )
                        }
                    },
                )

                if (savedSettings.useLiquidNavigation) {
                    AnimatedVisibility(
                        visible = isBarsVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        val currentScreen = backStack.last()
                        val currentTab = when {
                            currentScreen is Screen.Search || currentScreen is Screen.SongDetails -> TabEntry.SONGS
                            currentScreen is Screen.Favorites -> TabEntry.FAVORITES
                            currentScreen is Screen.Settings -> TabEntry.SETTINGS
                            else -> TabEntry.SONGS
                        }

                        val isLightTheme = when (savedSettings.themeMode) {
                            ThemeMode.LIGHT -> true
                            ThemeMode.DARK -> false
                            ThemeMode.SYSTEM -> !isSystemInDarkTheme()
                        }

                        LiquidBottomTabs(
                            selectedTab = { currentTab },
                            onTabSelected = { tab ->
                                val screen = when (tab) {
                                    TabEntry.SONGS -> Screen.Search
                                    TabEntry.FAVORITES -> Screen.Favorites
                                    TabEntry.SETTINGS -> Screen.Settings
                                }
                                if (backStack.last() != screen) {
                                    backStack.clear()
                                    backStack.add(screen)
                                }
                            },
                            fromIndex = { TabEntry.entries[it] },
                            backdrop = backdrop,
                            tabsCount = TabEntry.entries.size,
                            isLightTheme = isLightTheme,
                            modifier = Modifier
                                .widthIn(max = 128.dp * TabEntry.entries.size)
                                .padding(bottom = 24.dp)
                                .padding(horizontal = 16.dp)
                                .padding(padding),
                        ) {
                            val contentColor = MaterialTheme.colorScheme.onSurface
                            val iconColorFilter = ColorFilter.tint(contentColor)
                            TabEntry.entries.forEach { entry ->
                                LiquidBottomTab(
                                    onClick = {
                                        val screen = when (entry) {
                                            TabEntry.SONGS -> Screen.Search
                                            TabEntry.FAVORITES -> Screen.Favorites
                                            TabEntry.SETTINGS -> Screen.Settings
                                        }
                                        if (backStack.last() != screen) {
                                            backStack.clear()
                                            backStack.add(screen)
                                        }
                                    },
                                ) {
                                    val painter = rememberVectorPainter(image = entry.icon)
                                    Box(
                                        Modifier
                                            .size(24.dp)
                                            .paint(painter, colorFilter = iconColorFilter),
                                    )
                                    BasicText(
                                        entry.label,
                                        style = TextStyle(color = contentColor, fontSize = 12.sp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
