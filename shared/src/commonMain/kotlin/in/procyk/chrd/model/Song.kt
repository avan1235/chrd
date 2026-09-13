package `in`.procyk.chrd.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class SongListing(
    val author: String,
    val title: String,
    val source: Url,
    val origin: SongsOrigin,
    val song: Song? = null,
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

    fun transpose(delta: Int): LinePart

    @Serializable
    data class Lyric(
        val text: String,
    ) : LinePart {

        override fun transpose(delta: Int): Lyric = this
    }

    @Serializable
    data class ChordedLyric(
        val text: String,
        val chord: Chord,
    ) : LinePart {

        override fun transpose(delta: Int): ChordedLyric = copy(chord = chord.transpose(delta))
    }

    @Serializable
    data class ChordOverWhitespace(
        val chord: Chord,
    ) : LinePart {

        override fun transpose(delta: Int): ChordOverWhitespace = copy(chord = chord.transpose(delta))
    }

    @Serializable
    data class ChordInText(
        val chord: Chord,
    ) : LinePart {

        override fun transpose(delta: Int): ChordInText = copy(chord = chord.transpose(delta))
    }
}
