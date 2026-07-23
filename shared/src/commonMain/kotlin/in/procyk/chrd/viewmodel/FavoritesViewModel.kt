package `in`.procyk.chrd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.procyk.chrd.db.AppSettings
import `in`.procyk.chrd.db.AppSettingsRepository
import `in`.procyk.chrd.db.SongRepository
import `in`.procyk.chrd.model.SongListing
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val songRepository: SongRepository,
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    val useLiquidNavigation: StateFlow<Boolean> = settingsRepository.settings
        .map { it.useLiquidNavigation }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings.DEFAULT.useLiquidNavigation)

    val favorites: StateFlow<List<SongListing>> = songRepository.favorites
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun removeFavorite(songListing: SongListing) {
        viewModelScope.launch { songRepository.removeFavorite(songListing) }
    }
}