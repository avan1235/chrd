package `in`.procyk.chrd.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class SongListing(
    val author: String,
    val title: String,
    val source: Url,
    val origin: SongsOrigin,
)

@Serializable
data class Song(
    val author: String,
    val title: String,
    val sections: List<SongSection>,
)

@Serializable
data class SongSection(
    val type: SectionType,
    val lines: List<SongLine>,
)

@Serializable
enum class SectionType {
    VERSE, CHORUS, BRIDGE, OTHER,
}

@Serializable
data class SongLine(
    val parts: List<LinePart>,
)

@Serializable
sealed interface LinePart {
    data class Lyric(
        val text: String,
    ) : LinePart

    data class ChordedLyric(
        val text: String,
        val chord: String,
    ) : LinePart

    data class ChordOverWhitespace(
        val chord: Chord,
    ) : LinePart

    data class ChordInText(
        val chord: Chord,
    ) : LinePart
}
