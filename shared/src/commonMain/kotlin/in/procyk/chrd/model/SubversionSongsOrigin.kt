package `in`.procyk.chrd.model

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
class SubversionSongsOrigin(
    private val baseUrl: String,
) : SongsOrigin() {

    override suspend fun find(phrase: String): List<SongListing> {
        val url = buildUrl {
            takeFrom(baseUrl)
            path("search")
            encodedParameters.append("q", phrase)
        }
        val doc = parseGetRequest(url.toString())
        return parseSongListings(doc)
    }

    private fun parseSongListings(document: Document): List<SongListing> {
        val items = document.select("ul.grid-content > li.list-group-item")

        return items.mapNotNull { element ->
            val anchor = element.selectFirst("a") ?: return@mapNotNull null
            val url = anchor.attr("href")

            val (title, author) = anchor.text()
                .split(" - ", limit = 2)
                .takeIf { it.size == 2 }
                ?: return@mapNotNull null

            SongListing(
                title = title,
                author = author,
                source = Url(url),
                origin = this,
            )
        }
    }

    override suspend fun parseSong(document: Document): Song {
        val title = document.select("h1 > strong").text()
        val author = document.select("h1").first()?.text()?.removePrefix(title)?.trim().orEmpty()

        val content = document.select(".interpretation-content").single()

        val sections = mutableListOf<SongSection>()

        var currentType = SectionType.VERSE
        var currentLines = mutableListOf<SongLine>()
        var currentParts = mutableListOf<LinePart>()

        fun flushLine() {
            if (currentParts.isNotEmpty()) {
                while (currentParts.lastOrNull() is LinePart.Lyric && (currentParts.last() as LinePart.Lyric).text.isBlank()) {
                    currentParts.removeAt(currentParts.size - 1)
                }
                while (currentParts.firstOrNull() is LinePart.Lyric && (currentParts.first() as LinePart.Lyric).text.isBlank()) {
                    currentParts.removeAt(0)
                }
                if (currentParts.isNotEmpty()) {
                    val first = currentParts[0]
                    if (first is LinePart.Lyric) {
                        currentParts[0] = LinePart.Lyric(first.text.trimStart())
                    }
                    val last = currentParts.last()
                    if (last is LinePart.Lyric) {
                        currentParts[currentParts.size - 1] = LinePart.Lyric(last.text.trimEnd())
                    }
                    if (currentParts.isNotEmpty()) {
                        currentLines.add(SongLine(currentParts.toList()))
                    }
                }
                currentParts = mutableListOf()
            }
        }

        fun flushSection() {
            flushLine()

            if (currentLines.isNotEmpty()) {
                sections.add(
                    SongSection(
                        type = currentType,
                        lines = currentLines.toList(),
                    ),
                )
                currentLines = mutableListOf()
            }
            currentType = SectionType.VERSE
        }

        for (node in content.childNodes()) {
            when {
                node is Element && node.hasClass("song-section") -> {
                    flushSection()

                    val sectionText = node.text().trim()

                    currentType = when {
                        sectionText.contains("refren", ignoreCase = true) -> SectionType.CHORUS
                        sectionText.contains("bridge", ignoreCase = true) -> SectionType.BRIDGE
                        sectionText.contains("zwrotka", ignoreCase = true) -> SectionType.VERSE
                        else -> SectionType.OTHER
                    }
                }

                node.nodeName() == "br" -> {
                    if (currentParts.isEmpty()) {
                        flushSection()
                    } else {
                        flushLine()
                    }
                }

                node is Element && node.hasClass("annotated-lyrics") -> {
                    parseAnnotatedLyrics(node, currentParts)
                }

                node is Element && node.tagName() == "code" && node.hasAttr("data-chord") -> {
                    val chordText = node.text()
                    currentParts.add(LinePart.ChordInText(Chord(chordText)))
                }

                node is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank()) {
                        currentParts.add(LinePart.Lyric(text.trim()))
                    }
                }
            }
        }

        flushSection()

        return Song(
            author = author,
            title = title,
            sections = sections,
        )
    }

    private fun parseAnnotatedLyrics(
        element: Element,
        parts: MutableList<LinePart>,
    ) {
        var pendingChord: String? = null

        fun addLyric(text: String) {
            if (text.isEmpty()) return
            val last = parts.lastOrNull()
            if (last is LinePart.Lyric) {
                parts[parts.size - 1] = LinePart.Lyric(last.text + text)
            } else {
                parts.add(LinePart.Lyric(text))
            }
        }

        fun process(node: Node) {
            when (node) {
                is TextNode -> {
                    val text = node.getWholeText().replace("\u00A0", " ")
                    if (text.isEmpty()) return

                    if (pendingChord != null) {
                        val match = Regex("""^\s*\S+""").find(text)
                        if (match != null) {
                            val firstPart = match.value
                            val rest = text.substring(match.range.last + 1)
                            parts.add(LinePart.ChordedLyric(firstPart, Chord(pendingChord!!)))
                            pendingChord = null
                            addLyric(rest)
                        } else {
                            parts.add(LinePart.ChordOverWhitespace(Chord(pendingChord!!)))
                            pendingChord = null
                            addLyric(text)
                        }
                    } else {
                        addLyric(text)
                    }
                }

                is Element -> {
                    if (node.tagName() == "code" && node.hasClass("an")) {
                        if (pendingChord != null) {
                            parts.add(LinePart.ChordOverWhitespace(Chord(pendingChord!!)))
                        }
                        pendingChord = buildChord(node)
                    } else {
                        for (child in node.childNodes()) {
                            process(child)
                        }
                    }
                }
            }
        }

        for (child in element.childNodes()) {
            process(child)
        }
        if (pendingChord != null) {
            parts.add(LinePart.ChordOverWhitespace(Chord(pendingChord!!)))
        }
    }

    private fun buildChord(chordElement: Element): String {
        return chordElement.attr("data-local")
    }
}
