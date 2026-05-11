package `in`.procyk.chrd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.procyk.chrd.model.Song
import `in`.procyk.chrd.model.SongListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SongViewModel(
    private val listing: SongListing,
) : ViewModel() {

    val song: StateFlow<Song?>
        field = MutableStateFlow<Song?>(null)

    init {
        viewModelScope.launch {
            song.value = listing.origin.parseSong(listing)
        }
    }
}