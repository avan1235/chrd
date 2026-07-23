package `in`.procyk.chrd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.procyk.chrd.db.AppSettings
import `in`.procyk.chrd.db.AppSettingsRepository
import `in`.procyk.chrd.db.SongRepository
import `in`.procyk.chrd.model.Song
import `in`.procyk.chrd.model.SongListing
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SongViewModel(
    private val listing: SongListing,
    private val songRepository: SongRepository,
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    val song: StateFlow<Song?>
        field = MutableStateFlow<Song?>(listing.song)

    val isFavorite: StateFlow<Boolean> = songRepository.isFavorite(listing)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val useLiquidNavigation: StateFlow<Boolean> = settingsRepository.settings
        .map { it.useLiquidNavigation }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings.DEFAULT.useLiquidNavigation)

    init {
        if (song.value == null) {
            viewModelScope.launch {
                song.value = listing.origin.parseSong(listing)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentSong = song.value ?: return@launch
            if (isFavorite.value) {
                songRepository.removeFavorite(listing)
            } else {
                songRepository.addFavorite(listing, currentSong)
            }
        }
    }
}