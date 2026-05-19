package `in`.procyk.chrd.model

enum class Note(val semitones: Int) {
    C(0), C_SHARP(1), D(2), D_SHARP(3), E(4), F(5), F_SHARP(6), G(7), G_SHARP(8), A(9), A_SHARP(10), B(11);

    fun transpose(delta: Int): Note {
        val newSemitones = (semitones + delta).mod(12)
        return entries.first { it.semitones == newSemitones }
    }

    companion object {
        fun fromString(s: String): Note = when (s.uppercase()) {
            "C" -> C
            "C#", "DB" -> C_SHARP
            "D" -> D
            "D#", "EB" -> D_SHARP
            "E" -> E
            "F" -> F
            "F#", "GB" -> F_SHARP
            "G" -> G
            "G#", "AB" -> G_SHARP
            "A" -> A
            "A#", "BB" -> A_SHARP
            "B", "H" -> B
            else -> throw IllegalArgumentException("Unknown note: $s")
        }
    }

    override fun toString(): String = when (this) {
        C -> "C"
        C_SHARP -> "C#"
        D -> "D"
        D_SHARP -> "D#"
        E -> "E"
        F -> "F"
        F_SHARP -> "F#"
        G -> "G"
        G_SHARP -> "G#"
        A -> "A"
        A_SHARP -> "A#"
        B -> "B"
    }
}

enum class ChordQuality(val suffix: String) {
    MAJOR(""),
    MINOR("m"),
    DOMINANT_7("7"),
    MINOR_7("m7"),
    MAJOR_7("maj7"),
    SUS_4("sus4")
}

// 1. CHORD IS NOW A DATA CLASS
data class Chord(
    val root: Note,
    val quality: ChordQuality,
    val representation: String
) {
    val value: String get() = toString()

    fun transpose(delta: Int): Chord {
        val newRoot = root.transpose(delta)
        return fromRootAndQuality(newRoot, this.quality)
    }

    override fun toString(): String = "${root}${quality.suffix}"

    companion object {
        private val chordRegex = "^([A-Ha-h][#b]?)(.*)$".toRegex()

        // 2. THE CHORD DICTIONARY (Easy to maintain and read)
        private val voicingDictionary: Map<Pair<Note, ChordQuality>, String> = mapOf(
            // --- Major Chords ---
            Pair(Note.C, ChordQuality.MAJOR) to "x32010",
            Pair(Note.C_SHARP, ChordQuality.MAJOR) to "x46664",
            Pair(Note.D, ChordQuality.MAJOR) to "xx0232",
            Pair(Note.D_SHARP, ChordQuality.MAJOR) to "x68886",
            Pair(Note.E, ChordQuality.MAJOR) to "022100",
            Pair(Note.F, ChordQuality.MAJOR) to "133211",
            Pair(Note.F_SHARP, ChordQuality.MAJOR) to "244322",
            Pair(Note.G, ChordQuality.MAJOR) to "320003",
            Pair(Note.G_SHARP, ChordQuality.MAJOR) to "466544",
            Pair(Note.A, ChordQuality.MAJOR) to "x02220",
            Pair(Note.A_SHARP, ChordQuality.MAJOR) to "x13331",
            Pair(Note.B, ChordQuality.MAJOR) to "x24442",

            // --- Minor Chords ---
            Pair(Note.C, ChordQuality.MINOR) to "x35543",
            Pair(Note.C_SHARP, ChordQuality.MINOR) to "x46654",
            Pair(Note.D, ChordQuality.MINOR) to "xx0231",
            Pair(Note.D_SHARP, ChordQuality.MINOR) to "x68876",
            Pair(Note.E, ChordQuality.MINOR) to "022000",
            Pair(Note.F, ChordQuality.MINOR) to "133111",
            Pair(Note.F_SHARP, ChordQuality.MINOR) to "244222",
            Pair(Note.G, ChordQuality.MINOR) to "355333",
            Pair(Note.G_SHARP, ChordQuality.MINOR) to "466444",
            Pair(Note.A, ChordQuality.MINOR) to "x02210",
            Pair(Note.A_SHARP, ChordQuality.MINOR) to "x13321",
            Pair(Note.B, ChordQuality.MINOR) to "x24432",

            // --- Dominant 7th Chords ---
            Pair(Note.C, ChordQuality.DOMINANT_7) to "x32310",
            Pair(Note.C_SHARP, ChordQuality.DOMINANT_7) to "x46464",
            Pair(Note.D, ChordQuality.DOMINANT_7) to "xx0212",
            Pair(Note.D_SHARP, ChordQuality.DOMINANT_7) to "x68686",
            Pair(Note.E, ChordQuality.DOMINANT_7) to "020100",
            Pair(Note.F, ChordQuality.DOMINANT_7) to "131211",
            Pair(Note.F_SHARP, ChordQuality.DOMINANT_7) to "242322",
            Pair(Note.G, ChordQuality.DOMINANT_7) to "320001",
            Pair(Note.G_SHARP, ChordQuality.DOMINANT_7) to "464544",
            Pair(Note.A, ChordQuality.DOMINANT_7) to "x02020",
            Pair(Note.A_SHARP, ChordQuality.DOMINANT_7) to "x13131",
            Pair(Note.B, ChordQuality.DOMINANT_7) to "x21202",

            // --- Minor 7th Chords ---
            Pair(Note.C, ChordQuality.MINOR_7) to "x35343",
            Pair(Note.C_SHARP, ChordQuality.MINOR_7) to "x46454",
            Pair(Note.D, ChordQuality.MINOR_7) to "xx0211",
            Pair(Note.D_SHARP, ChordQuality.MINOR_7) to "x68676",
            Pair(Note.E, ChordQuality.MINOR_7) to "020000",
            Pair(Note.F, ChordQuality.MINOR_7) to "131111",
            Pair(Note.F_SHARP, ChordQuality.MINOR_7) to "242222",
            Pair(Note.G, ChordQuality.MINOR_7) to "353333",
            Pair(Note.G_SHARP, ChordQuality.MINOR_7) to "464444",
            Pair(Note.A, ChordQuality.MINOR_7) to "x02010",
            Pair(Note.A_SHARP, ChordQuality.MINOR_7) to "x13121",
            Pair(Note.B, ChordQuality.MINOR_7) to "x24232",

            // --- Major 7th Chords ---
            Pair(Note.C, ChordQuality.MAJOR_7) to "x32000",
            Pair(Note.C_SHARP, ChordQuality.MAJOR_7) to "x46564",
            Pair(Note.D, ChordQuality.MAJOR_7) to "xx0222",
            Pair(Note.D_SHARP, ChordQuality.MAJOR_7) to "x68786",
            Pair(Note.E, ChordQuality.MAJOR_7) to "021100",
            Pair(Note.F, ChordQuality.MAJOR_7) to "132211",
            Pair(Note.F_SHARP, ChordQuality.MAJOR_7) to "243322",
            Pair(Note.G, ChordQuality.MAJOR_7) to "320002",
            Pair(Note.G_SHARP, ChordQuality.MAJOR_7) to "465544",
            Pair(Note.A, ChordQuality.MAJOR_7) to "x02120",
            Pair(Note.A_SHARP, ChordQuality.MAJOR_7) to "x13231",
            Pair(Note.B, ChordQuality.MAJOR_7) to "x24342",

            // --- Sus4 Chords ---
            Pair(Note.C, ChordQuality.SUS_4) to "x35563",
            Pair(Note.C_SHARP, ChordQuality.SUS_4) to "x46674",
            Pair(Note.D, ChordQuality.SUS_4) to "xx0233",
            Pair(Note.D_SHARP, ChordQuality.SUS_4) to "x68896",
            Pair(Note.E, ChordQuality.SUS_4) to "022200",
            Pair(Note.F, ChordQuality.SUS_4) to "133311",
            Pair(Note.F_SHARP, ChordQuality.SUS_4) to "244422",
            Pair(Note.G, ChordQuality.SUS_4) to "355533",
            Pair(Note.G_SHARP, ChordQuality.SUS_4) to "466644",
            Pair(Note.A, ChordQuality.SUS_4) to "x02230",
            Pair(Note.A_SHARP, ChordQuality.SUS_4) to "x13341",
            Pair(Note.B, ChordQuality.SUS_4) to "x24452"
        )

        val C = fromRootAndQuality(Note.C, ChordQuality.MAJOR)
        val Cm = fromRootAndQuality(Note.C, ChordQuality.MINOR)
        val C_SHARP = fromRootAndQuality(Note.C_SHARP, ChordQuality.MAJOR)
        val C_SHARP_m = fromRootAndQuality(Note.C_SHARP, ChordQuality.MINOR)
        val D = fromRootAndQuality(Note.D, ChordQuality.MAJOR)
        val Dm = fromRootAndQuality(Note.D, ChordQuality.MINOR)
        val D_SHARP = fromRootAndQuality(Note.D_SHARP, ChordQuality.MAJOR)
        val D_SHARP_m = fromRootAndQuality(Note.D_SHARP, ChordQuality.MINOR)
        val E = fromRootAndQuality(Note.E, ChordQuality.MAJOR)
        val Em = fromRootAndQuality(Note.E, ChordQuality.MINOR)
        val F = fromRootAndQuality(Note.F, ChordQuality.MAJOR)
        val Fm = fromRootAndQuality(Note.F, ChordQuality.MINOR)
        val F_SHARP = fromRootAndQuality(Note.F_SHARP, ChordQuality.MAJOR)
        val F_SHARP_m = fromRootAndQuality(Note.F_SHARP, ChordQuality.MINOR)
        val G = fromRootAndQuality(Note.G, ChordQuality.MAJOR)
        val Gm = fromRootAndQuality(Note.G, ChordQuality.MINOR)
        val G_SHARP = fromRootAndQuality(Note.G_SHARP, ChordQuality.MAJOR)
        val G_SHARP_m = fromRootAndQuality(Note.G_SHARP, ChordQuality.MINOR)
        val A = fromRootAndQuality(Note.A, ChordQuality.MAJOR)
        val Am = fromRootAndQuality(Note.A, ChordQuality.MINOR)
        val A_SHARP = fromRootAndQuality(Note.A_SHARP, ChordQuality.MAJOR)
        val A_SHARP_m = fromRootAndQuality(Note.A_SHARP, ChordQuality.MINOR)
        val B = fromRootAndQuality(Note.B, ChordQuality.MAJOR)
        val Bm = fromRootAndQuality(Note.B, ChordQuality.MINOR)

        fun fromRootAndQuality(root: Note, quality: ChordQuality): Chord {
            val representation = voicingDictionary[Pair(root, quality)]
                ?: throw IllegalArgumentException("Chord voicing not supported in dictionary: ${root}${quality.suffix}")
            return Chord(root, quality, representation)
        }

        fun fromString(s: String): Chord {
            val match = chordRegex.find(s)
                ?: throw IllegalArgumentException("Invalid chord format: $s")

            val rootStr = match.groupValues[1]
            val suffixStr = match.groupValues[2]

            val root = Note.fromString(rootStr)
            val quality = if (rootStr[0].isLowerCase() && suffixStr.isEmpty()) {
                ChordQuality.MINOR
            } else {
                ChordQuality.entries.firstOrNull { it.suffix.equals(suffixStr, ignoreCase = true) }
                    ?: throw IllegalArgumentException("Unsupported chord quality: '$suffixStr' in chord '$s'")
            }

            return fromRootAndQuality(root, quality)
        }

        operator fun invoke(value: String): Chord = fromString(value)
    }
}