package `in`.procyk.chrd.model

import com.fleeksoft.ksoup.nodes.Document
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class UltimateSongsOrigin(
    override val baseUrl: String,
) : SongsOrigin() {

    override suspend fun find(phrase: String): List<SongListing> {
        val url = buildUrl {
            takeFrom(baseUrl)
            path("search.php")
            encodedParameters.apply {
                append("title", phrase)
                append("type", "300")
            }
        }
        val doc = parseGetRequest(url.toString())
        return parseSongListings(doc)
    }

    private fun parseSongListings(document: Document): List<SongListing> {
        val dataContent = document.extractJsStoreData() ?: return emptyList()
        val searchStore = json.decodeFromString<UltimateStore<UltimateSearchData>>(dataContent)
        val results = searchStore.store?.page?.data?.results ?: return emptyList()
        return results.mapNotNull { result ->
            val songName = result.songName
                ?: result.localizedSongName
                ?: return@mapNotNull null
            val artistName = result.artistName
                ?: result.localizedArtistName
                ?: return@mapNotNull null
            val tabUrl = result.tabUrl
                ?: return@mapNotNull null
            SongListing(
                title = songName,
                author = artistName,
                source = Url(tabUrl),
                origin = this,
            )
        }
    }

    override suspend fun parseSong(document: Document): Song {
        val dataContent = document.extractJsStoreData() ?: error("No .js-store found in document")

        val songStore = json.decodeFromString<UltimateStore<UltimateSongData>>(dataContent)
        val songData = songStore.store?.page?.data

        val tab = songData?.tab
        val title = tab?.songName
            ?: tab?.localizedSongName
            ?: document.selectFirst("h1")?.text()
                .orEmpty()

        val author = tab?.artistName
            ?: tab?.localizedArtistName
                .orEmpty()

        val content = songData?.tabView?.wikiTab?.content.orEmpty()
        val sections = parseWikiTabContent(content)

        return Song(
            author = author,
            title = title,
            sections = sections,
        )
    }

    private fun parseWikiTabContent(content: String): List<SongSection> = buildList {
        val cleanContent = content
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("[tab]", "")
            .replace("[/tab]", "")

        val lines = cleanContent.lines()
        var currentType = SectionType.VERSE
        val currentLines = mutableListOf<SongLine>()

        fun flushSection() {
            if (currentLines.isNotEmpty()) {
                add(
                    SongSection(
                        type = currentType,
                        lines = currentLines.toList(),
                    ),
                )
                currentLines.clear()
            }
        }

        var lineIdx = 0
        while (lineIdx < lines.size) {
            val rawLine = lines[lineIdx].trimEnd()
            lineIdx++

            if (rawLine.isBlank()) continue

            if (isSectionHeader(rawLine)) {
                flushSection()
                currentType = parseSectionType(rawLine)
                continue
            }

            val containsChords = rawLine.contains("[ch]")
            val textWithoutChords = rawLine.replace(CHORD_REGEX, "")
            val isPureChordLine = containsChords && textWithoutChords.isBlank()

            if (isPureChordLine) {
                val nextRawLine = lines.getOrNull(lineIdx)?.trimEnd()
                val nextIsLyric = !nextRawLine.isNullOrBlank() &&
                        !nextRawLine.contains("[ch]") &&
                        !isSectionHeader(nextRawLine)

                if (nextIsLyric) {
                    lineIdx++
                    currentLines.add(parseChordAboveLyricLine(rawLine, nextRawLine))
                } else {
                    currentLines.add(parseChordOnlyLine(rawLine))
                }
            } else if (containsChords) {
                currentLines.add(parseInlineChordLine(rawLine))
            } else {
                currentLines.add(SongLine(listOf(LinePart.Lyric(rawLine))))
            }
        }

        flushSection()
    }

    private fun isSectionHeader(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith("[") &&
                trimmed.endsWith("]") &&
                !trimmed.contains("[ch]") &&
                !trimmed.contains("[/ch]") &&
                !trimmed.contains("[tab]") &&
                !trimmed.contains("[/tab]")
    }

    private fun parseSectionType(headerText: String): SectionType {
        val name = headerText.removePrefix("[").removeSuffix("]").trim()
        return when {
            name.contains("chorus", ignoreCase = true) || name.contains(
                "refren",
                ignoreCase = true
            ) -> SectionType.CHORUS

            name.contains("verse", ignoreCase = true) || name.contains(
                "zwrotka",
                ignoreCase = true
            ) -> SectionType.VERSE

            name.contains("bridge", ignoreCase = true) -> SectionType.BRIDGE
            else -> SectionType.OTHER
        }
    }

    private fun parseChordAboveLyricLine(chordLine: String, lyricLine: String): SongLine {
        val chords = buildList {
            var plainIndex = 0
            var lastMatchEnd = 0
            for (match in CHORD_REGEX.findAll(chordLine)) {
                val prefixLen = match.range.first - lastMatchEnd
                plainIndex += prefixLen
                val chordName = match.groupValues[1]
                add(plainIndex to chordName)
                plainIndex += chordName.length
                lastMatchEnd = match.range.last + 1
            }
        }

        val parts = buildList {
            var currentIndex = 0
            for (i in chords.indices) {
                val (chordCol, chordName) = chords[i]
                val nextChordCol = chords.getOrNull(i + 1)?.first ?: Int.MAX_VALUE

                if (chordCol > currentIndex && currentIndex < lyricLine.length) {
                    val beforeSlice = lyricLine.substring(currentIndex, minOf(chordCol, lyricLine.length))
                    if (beforeSlice.isNotEmpty()) {
                        add(LinePart.Lyric(beforeSlice))
                    }
                    currentIndex = minOf(chordCol, lyricLine.length)
                }

                val chord = Chord(chordName)
                if (chordCol < lyricLine.length) {
                    val endIdx = minOf(nextChordCol, lyricLine.length)
                    val slice = lyricLine.substring(chordCol, endIdx)
                    add(LinePart.ChordedLyric(slice, chord))
                    currentIndex = endIdx
                } else {
                    add(LinePart.ChordOverWhitespace(chord))
                }
            }

            if (currentIndex < lyricLine.length) {
                val remaining = lyricLine.substring(currentIndex)
                if (remaining.isNotEmpty()) {
                    add(LinePart.Lyric(remaining))
                }
            }
        }

        return SongLine(parts)
    }

    private fun parseChordOnlyLine(chordLine: String): SongLine =
        SongLine(CHORD_REGEX.findAll(chordLine).map { LinePart.ChordInText(Chord(it.groupValues[1])) }.toList())

    private fun parseInlineChordLine(line: String): SongLine {
        val parts = buildList {
            var lastIndex = 0
            for (match in CHORD_REGEX.findAll(line)) {
                if (match.range.first > lastIndex) {
                    val text = line.substring(lastIndex, match.range.first)
                    if (text.isNotEmpty()) {
                        add(LinePart.Lyric(text))
                    }
                }
                add(LinePart.ChordInText(Chord(match.groupValues[1])))
                lastIndex = match.range.last + 1
            }
            if (lastIndex < line.length) {
                val text = line.substring(lastIndex)
                if (text.isNotEmpty()) {
                    add(LinePart.Lyric(text))
                }
            }
        }
        return SongLine(parts)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private val CHORD_REGEX = Regex("""\[ch](.*?)\[/ch]""")
    }
}

private fun Document.extractJsStoreData(): String? =
    selectFirst(".js-store")?.attr("data-content")


@Serializable
private data class UltimateStore<T>(
    @SerialName("store") val store: PageStore<T>? = null,
) {
    @Serializable
    data class PageStore<T>(
        @SerialName("page") val page: Page<T>? = null,
    )

    @Serializable
    data class Page<T>(
        @SerialName("data") val data: T? = null,
    )
}

@Serializable
private data class UltimateSearchData(
    @SerialName("results") val results: List<UltimateSearchResultDto>? = null,
)

@Serializable
private data class UltimateSearchResultDto(
    @SerialName("song_name") val songName: String? = null,
    @SerialName("localized_song_name") val localizedSongName: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("localized_artist_name") val localizedArtistName: String? = null,
    @SerialName("tab_url") val tabUrl: String? = null,
)

@Serializable
private data class UltimateSongData(
    @SerialName("tab") val tab: UltimateTabDto? = null,
    @SerialName("tab_view") val tabView: UltimateTabViewDto? = null,
)

@Serializable
private data class UltimateTabDto(
    @SerialName("song_name") val songName: String? = null,
    @SerialName("localized_song_name") val localizedSongName: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("localized_artist_name") val localizedArtistName: String? = null,
)

@Serializable
private data class UltimateTabViewDto(
    @SerialName("wiki_tab") val wikiTab: UltimateWikiTabDto? = null,
)

@Serializable
private data class UltimateWikiTabDto(
    @SerialName("content") val content: String? = null,
)
