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

enum class Chord(
    val root: Note,
    val isMinor: Boolean,
    /**
     * A 6-character string representing the guitar fretboard fingering.
     *
     * The characters correspond to the strings from lowest to highest pitch (E, A, D, G, B, e).
     * - 'x': The string is muted or not played.
     * - '0': An open string.
     * - '1'-'9': The fret number to be pressed.
     */
    val representation: String
) {
    C(Note.C, false, "x32010"),
    Cm(Note.C, true, "x35543"),
    C_SHARP(Note.C_SHARP, false, "x46664"),
    C_SHARP_m(Note.C_SHARP, true, "x46654"),
    D(Note.D, false, "xx0232"),
    Dm(Note.D, true, "xx0231"),
    D_SHARP(Note.D_SHARP, false, "x68886"),
    D_SHARP_m(Note.D_SHARP, true, "x68876"),
    E(Note.E, false, "022100"),
    Em(Note.E, true, "022000"),
    F(Note.F, false, "133211"),
    Fm(Note.F, true, "133111"),
    F_SHARP(Note.F_SHARP, false, "244322"),
    F_SHARP_m(Note.F_SHARP, true, "244222"),
    G(Note.G, false, "320003"),
    Gm(Note.G, true, "355333"),
    G_SHARP(Note.G_SHARP, false, "466544"),
    G_SHARP_m(Note.G_SHARP, true, "466444"),
    A(Note.A, false, "x02220"),
    Am(Note.A, true, "x02210"),
    A_SHARP(Note.A_SHARP, false, "x13331"),
    A_SHARP_m(Note.A_SHARP, true, "x13321"),
    B(Note.B, false, "x24442"),
    Bm(Note.B, true, "x24432");

    val value: String get() = toString()

    fun transpose(delta: Int): Chord {
        val newRoot = root.transpose(delta)
        return entries.first { it.root == newRoot && it.isMinor == this.isMinor }
    }

    companion object {
        fun fromString(s: String): Chord {
            val isMinor = s.endsWith("m") || s[0].isLowerCase()
            val rootStr = if (s.endsWith("m")) s.substring(0, s.length - 1) else s
            val root = Note.fromString(rootStr)
            return entries.firstOrNull { it.root == root && it.isMinor == isMinor }
                ?: throw IllegalArgumentException("Chord not supported: $s")
        }

        operator fun invoke(value: String): Chord = fromString(value)
    }

    override fun toString(): String = "${root}${if (isMinor) "m" else ""}"
}
