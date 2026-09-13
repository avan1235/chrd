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

    private val _transposeDelta = MutableStateFlow(0)

    private val _song = MutableStateFlow(listing.song)
    val song: StateFlow<Song?> = combine(_song, _transposeDelta) { song, transposeDelta ->
        when (transposeDelta) {
            0 -> song
            else -> song?.run {
                copy(sections = sections.map { section ->
                    section.copy(lines = section.lines.map { line ->
                        line.copy(parts = line.parts.map { part ->
                            part.transpose(transposeDelta)
                        })
                    })
                })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, listing.song)

    val isFavorite: StateFlow<Boolean> = songRepository.isFavorite(listing)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val useLiquidNavigation: StateFlow<Boolean> = settingsRepository.settings
        .map { it.useLiquidNavigation }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings.DEFAULT.useLiquidNavigation)

    init {
        if (_song.value == null) {
            viewModelScope.launch {
                _song.value = listing.origin.parseSong(listing)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentSong = _song.value ?: return@launch
            if (isFavorite.value) {
                songRepository.removeFavorite(listing)
            } else {
                songRepository.addFavorite(listing, currentSong)
            }
        }
    }

    fun halfToneUp() {
        _transposeDelta.update { it + 1 }
    }

    fun halfToneDown() {
        _transposeDelta.update { it - 1 }
    }
}