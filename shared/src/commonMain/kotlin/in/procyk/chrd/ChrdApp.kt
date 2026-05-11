package `in`.procyk.chrd

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import `in`.procyk.chrd.screen.Screen
import `in`.procyk.chrd.screen.SearchScreen
import `in`.procyk.chrd.screen.SongScreen
import `in`.procyk.chrd.viewmodel.SearchViewModel
import `in`.procyk.chrd.viewmodel.SongViewModel

@Composable
@Preview
fun ChrdApp() {
    ChrdTheme {

        val backStack = rememberNavBackStack(Screen.SavedStateConfiguration, Screen.Search)

        NavDisplay(
            modifier = Modifier
                .safeContentPadding()
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