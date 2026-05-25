package `in`.procyk.chrd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.procyk.chrd.db.SongRepository
import `in`.procyk.chrd.model.Song
import `in`.procyk.chrd.model.SongListing
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SongViewModel(
    private val listing: SongListing,
    private val repository: SongRepository,
) : ViewModel() {

    val song: StateFlow<Song?>
        field = MutableStateFlow<Song?>(listing.song)

    val isFavorite: StateFlow<Boolean> = repository.isFavorite(listing)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
                repository.removeFavorite(listing)
            } else {
                repository.addFavorite(listing, currentSong)
            }
        }
    }
}