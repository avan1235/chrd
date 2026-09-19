package `in`.procyk.chrd.model

import com.fleeksoft.ksoup.nodes.Document
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
class UltimateSongsOrigin(
    private val baseUrl: String,
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
        val jsStore = document.select(".js-store").firstOrNull()
        val dataContent = jsStore?.attr("data-content") ?: return emptyList()
        val root = json.parseToJsonElement(dataContent) as JsonObject
        val store = root["store"] as? JsonObject
        val page = store?.get("page") as? JsonObject
        val data = page?.get("data") as? JsonObject
        val results = data?.get("results") as? JsonArray ?: return emptyList()
        return results.mapNotNull { result ->
            val obj = result as? JsonObject ?: return@mapNotNull null
            val songName = (obj["song_name"] as? JsonPrimitive)?.content
                ?: (obj["localized_song_name"] as? JsonPrimitive)?.content
                ?: return@mapNotNull null
            val artistName = (obj["artist_name"] as? JsonPrimitive)?.content
                ?: (obj["localized_artist_name"] as? JsonPrimitive)?.content
                ?: return@mapNotNull null
            val tabUrl = (obj["tab_url"] as? JsonPrimitive)?.content
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
        val jsStore = document.select(".js-store").firstOrNull()
        val dataContent = jsStore?.attr("data-content") ?: error("No .js-store found in document")

        val root = json.parseToJsonElement(dataContent) as JsonObject
        val store = root["store"] as? JsonObject
        val page = store?.get("page") as? JsonObject
        val data = page?.get("data") as? JsonObject

        val tab = data?.get("tab") as? JsonObject
        val tabView = data?.get("tab_view") as? JsonObject
        val wikiTab = tabView?.get("wiki_tab") as? JsonObject

        val title = (tab?.get("song_name") as? JsonPrimitive)?.content
            ?: (tab?.get("localized_song_name") as? JsonPrimitive)?.content
            ?: document.select("h1").firstOrNull()?.text()
            ?: ""

        val author = (tab?.get("artist_name") as? JsonPrimitive)?.content
            ?: (tab?.get("localized_artist_name") as? JsonPrimitive)?.content
            ?: ""

        val content = (wikiTab?.get("content") as? JsonPrimitive)?.content ?: ""

        val sections = parseWikiTabContent(content)

        return Song(
            author = author,
            title = title,
            sections = sections,
        )
    }

    private fun parseWikiTabContent(content: String): List<SongSection> {
        val cleanContent = content
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("[tab]", "")
            .replace("[/tab]", "")

        val lines = cleanContent.lines()
        val sections = mutableListOf<SongSection>()

        var currentType = SectionType.VERSE
        val currentLines = mutableListOf<SongLine>()

        fun flushSection() {
            if (currentLines.isNotEmpty()) {
                sections.add(
                    SongSection(
                        type = currentType,
                        lines = currentLines.toList(),
                    ),
                )
                currentLines.clear()
            }
        }

        fun isSectionHeader(line: String): Boolean {
            val trimmed = line.trim()
            return trimmed.startsWith("[") &&
                    trimmed.endsWith("]") &&
                    !trimmed.contains("[ch]") &&
                    !trimmed.contains("[/ch]") &&
                    !trimmed.contains("[tab]") &&
                    !trimmed.contains("[/tab]")
        }

        fun parseSectionType(headerText: String): SectionType {
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
            val textWithoutChords = rawLine.replace(Regex("""\[ch].*?\[/ch]"""), "")
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
        return sections
    }

    private fun parseChordAboveLyricLine(chordLine: String, lyricLine: String): SongLine {
        val chords = mutableListOf<Pair<Int, String>>()
        val chordRegex = Regex("""\[ch](.*?)\[/ch]""")
        var plainIndex = 0
        var lastMatchEnd = 0

        for (match in chordRegex.findAll(chordLine)) {
            val prefixLen = match.range.first - lastMatchEnd
            plainIndex += prefixLen
            val chordName = match.groupValues[1]
            chords.add(plainIndex to chordName)
            plainIndex += chordName.length
            lastMatchEnd = match.range.last + 1
        }

        val parts = mutableListOf<LinePart>()
        var currentIndex = 0

        for (i in chords.indices) {
            val (chordCol, chordName) = chords[i]
            val nextChordCol = if (i + 1 < chords.size) chords[i + 1].first else Int.MAX_VALUE

            if (chordCol > currentIndex) {
                if (currentIndex < lyricLine.length) {
                    val beforeSlice = lyricLine.substring(currentIndex, minOf(chordCol, lyricLine.length))
                    if (beforeSlice.isNotEmpty()) {
                        parts.add(LinePart.Lyric(beforeSlice))
                    }
                    currentIndex = minOf(chordCol, lyricLine.length)
                }
            }

            val chord = Chord(chordName)
            if (chordCol < lyricLine.length) {
                val endIdx = minOf(nextChordCol, lyricLine.length)
                val slice = lyricLine.substring(chordCol, endIdx)
                parts.add(LinePart.ChordedLyric(slice, chord))
                currentIndex = endIdx
            } else {
                parts.add(LinePart.ChordOverWhitespace(chord))
            }
        }

        if (currentIndex < lyricLine.length) {
            val remaining = lyricLine.substring(currentIndex)
            if (remaining.isNotEmpty()) {
                parts.add(LinePart.Lyric(remaining))
            }
        }

        return SongLine(parts)
    }

    private fun parseChordOnlyLine(chordLine: String): SongLine {
        val chordRegex = Regex("""\[ch](.*?)\[/ch]""")
        val parts = chordRegex.findAll(chordLine).map {
            LinePart.ChordInText(Chord(it.groupValues[1]))
        }.toList()
        return SongLine(parts)
    }

    private fun parseInlineChordLine(line: String): SongLine {
        val chordRegex = Regex("""\[ch](.*?)\[/ch]""")
        val parts = mutableListOf<LinePart>()
        var lastIndex = 0
        for (match in chordRegex.findAll(line)) {
            if (match.range.first > lastIndex) {
                val text = line.substring(lastIndex, match.range.first)
                if (text.isNotEmpty()) {
                    parts.add(LinePart.Lyric(text))
                }
            }
            parts.add(LinePart.ChordInText(Chord(match.groupValues[1])))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < line.length) {
            val text = line.substring(lastIndex)
            if (text.isNotEmpty()) {
                parts.add(LinePart.Lyric(text))
            }
        }
        return SongLine(parts)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }

}
