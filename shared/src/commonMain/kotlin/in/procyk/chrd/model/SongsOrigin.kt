package `in`.procyk.chrd.model

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.fleeksoft.ksoup.nodes.Document
import `in`.procyk.chrd.http.ChrdHttpClient
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class SongsOrigin : AutoCloseable {

    @Transient
    protected val httpClient: HttpClient = ChrdHttpClient()

    abstract suspend fun find(phrase: String): List<SongListing>

    suspend fun parseSong(listing: SongListing): Song {
        val document = parseGetRequest(url = listing.source.toString())
        return parseSong(document)
    }

    abstract suspend fun parseSong(document: Document): Song

    override fun close() {
        httpClient.close()
    }

    protected suspend fun parseGetRequest(url: String): Document =
        Ksoup.parseGetRequest(url = url, httpClient = httpClient)
}