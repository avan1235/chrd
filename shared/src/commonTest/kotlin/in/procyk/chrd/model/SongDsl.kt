package `in`.procyk.chrd.model

fun song(author: String, title: String, block: SongBuilder.() -> Unit): Song =
    SongBuilder(author, title).apply(block).build()

class SongBuilder(private val author: String, private val title: String) {
    private val sections = mutableListOf<SongSection>()

    fun section(type: SectionType, block: SectionBuilder.() -> Unit) {
        sections.add(SectionBuilder(type).apply(block).build())
    }

    fun v(block: SectionBuilder.() -> Unit) = section(SectionType.VERSE, block)

    fun c(block: SectionBuilder.() -> Unit) = section(SectionType.CHORUS, block)

    fun b(block: SectionBuilder.() -> Unit) = section(SectionType.BRIDGE, block)

    fun o(block: SectionBuilder.() -> Unit) = section(SectionType.OTHER, block)

    fun verse(block: SectionBuilder.() -> Unit) = v(block)

    fun chorus(block: SectionBuilder.() -> Unit) = c(block)

    fun bridge(block: SectionBuilder.() -> Unit) = b(block)

    fun other(block: SectionBuilder.() -> Unit) = o(block)

    fun build() = Song(author, title, sections)
}

class SectionBuilder(private val type: SectionType) {
    private val lines = mutableListOf<SongLine>()

    fun l(block: LineBuilder.() -> Unit) {
        lines.add(LineBuilder().apply(block).build())
    }

    fun line(block: LineBuilder.() -> Unit) = l(block)

    fun build() = SongSection(type, lines)
}

class LineBuilder {
    private val parts = mutableListOf<LinePart>()

    operator fun String.unaryPlus() {
        parts.add(LinePart.Lyric(this))
    }

    infix fun String.c(chord: String) {
        parts.add(LinePart.ChordedLyric(this, Chord(chord)))
    }

    fun lyric(text: String) {
        +text
    }

    fun l(text: String) {
        +text
    }

    fun chordedLyric(text: String, chord: String) {
        text c chord
    }

    fun cw(chord: String) {
        parts.add(LinePart.ChordOverWhitespace(Chord(chord)))
    }

    fun chordOverWhitespace(chord: String) {
        cw(chord)
    }

    fun ci(chord: String) {
        parts.add(LinePart.ChordInText(Chord(chord)))
    }

    fun chordInText(chord: String) {
        ci(chord)
    }

    fun build() = SongLine(parts)
}
