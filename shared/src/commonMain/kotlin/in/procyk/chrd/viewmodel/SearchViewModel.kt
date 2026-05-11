package `in`.procyk.chrd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.procyk.chrd.model.SongListing
import `in`.procyk.chrd.model.SongsOrigin
import `in`.procyk.chrd.model.SubversionSongsOrigin
import `in`.procyk.chrd.shared.ChrdSharedConfig
import `in`.procyk.chrd.viewmodel.SearchRequest.Init
import `in`.procyk.chrd.viewmodel.SearchRequest.Search
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    private val origins = listOf<SongsOrigin>(
        SubversionSongsOrigin(ChrdSharedConfig.SUBVERSION_PL_SONGS_ORIGIN_URL),
        SubversionSongsOrigin(ChrdSharedConfig.SUBVERSION_EN_SONGS_ORIGIN_URL),
    )

    val query: StateFlow<String>
        field = MutableStateFlow("")

    val results: StateFlow<List<SongListing>>
        field = MutableStateFlow(emptyList())

    val isLoadingSongs: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private val searchRequests = MutableStateFlow<SearchRequest>(Init)

    init {
        viewModelScope.launch {
            searchRequests.debounce(1.seconds).collectLatest { request ->
                when (request) {
                    Init -> {}
                    is Search -> {
                        val startedLoading = Clock.System.now()
                        try {
                            isLoadingSongs.value = true
                            results.value = origins.map {
                                async { it.find(request.phrase) }
                            }.awaitAll().flatten()
                        } finally {
                            val loadingTime = Clock.System.now() - startedLoading
                            if (loadingTime < MIN_SONGS_LOADING_ANIMATION) {
                                val left = MIN_SONGS_LOADING_ANIMATION - loadingTime
                                delay(left)
                            }
                            isLoadingSongs.value = false
                        }
                    }
                }
            }
        }
    }

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun onRequestSearch() {
        val phrase = query.value
        searchRequests.update { Search(it.id + 1, phrase) }
    }
}

private sealed class SearchRequest {
    abstract val id: Long

    data object Init : SearchRequest() {
        override val id: Long = 0
    }

    data class Search(
        override val id: Long,
        val phrase: String,
    ) : SearchRequest()
}

private val MIN_SONGS_LOADING_ANIMATION = 1.seconds