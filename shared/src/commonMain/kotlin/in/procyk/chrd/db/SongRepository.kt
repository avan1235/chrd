package `in`.procyk.chrd.db

import `in`.procyk.chrd.model.Song
import `in`.procyk.chrd.model.SongListing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SongRepository(private val dao: SongDao) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    val favorites: Flow<List<SongListing>> =
        dao.observeAll().map { entities ->
            entities.map { it.toListing(json) }
        }

    fun isFavorite(listing: SongListing): Flow<Boolean> =
        dao.isFavorite(listing.source.toString())

    suspend fun addFavorite(listing: SongListing, song: Song) {
        dao.upsert(
            SongEntity(
                source = listing.source.toString(),
                author = listing.author,
                title = listing.title,
                contentJson = json.encodeToString(song),
                listingJson = json.encodeToString(listing),
            )
        )
    }

    suspend fun removeFavorite(listing: SongListing) {
        dao.deleteBySource(listing.source.toString())
    }

    suspend fun getFavoriteSong(listing: SongListing): Song? {
        return dao.observeBySource(listing.source.toString()).firstOrNull()?.toListing(json)?.song
    }
}

private fun SongEntity.toListing(json: Json): SongListing {
    val listing = json.decodeFromString<SongListing>(listingJson)
    val song = json.decodeFromString<Song>(contentJson)
    return listing.copy(song = song)
}
