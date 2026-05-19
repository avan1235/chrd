package `in`.procyk.chrd.model

import kotlin.test.*

class ChordTest {

    @Test
    fun testChordFromString() {
        assertEquals(Chord.C, Chord.fromString("C"))
        assertEquals(Chord.Cm, Chord.fromString("Cm"))
        assertEquals(Chord.C_SHARP, Chord.fromString("C#"))
        assertEquals(Chord.C_SHARP, Chord.fromString("Db"))
        assertEquals(Chord.C_SHARP_m, Chord.fromString("C#m"))
        assertEquals(Chord.C_SHARP_m, Chord.fromString("Dbm"))

        // Polish and lowercase notation
        assertEquals(Chord.Em, Chord.fromString("e"))
        assertEquals(Chord.Am, Chord.fromString("a"))
        assertEquals(Chord.B, Chord.fromString("H"))
        assertEquals(Chord.Bm, Chord.fromString("h"))

        assertFailsWith<IllegalArgumentException> {
            Chord.fromString("Unknown")
        }
    }

    @Test
    fun testChordInvoke() {
        assertEquals(Chord.C, Chord("C"))
    }

    @Test
    fun testTranspose() {
        assertEquals(Chord.D, Chord.C.transpose(2))
        assertEquals(Chord.Dm, Chord.Cm.transpose(2))
        assertEquals(Chord.C, Chord.B.transpose(1))
        assertEquals(Chord.B, Chord.C.transpose(-1))
        assertEquals(Chord.G, Chord.C.transpose(7))
    }

    @Test
    fun testRepresentation() {
        assertEquals("x32010", Chord.C.representation)
        assertEquals("x35543", Chord.Cm.representation)
        assertEquals("xx0232", Chord.D.representation)
        assertEquals("xx0231", Chord.Dm.representation)
        assertEquals("022100", Chord.E.representation)
        assertEquals("133211", Chord.F.representation)
        assertEquals("320003", Chord.G.representation)
        assertEquals("x02220", Chord.A.representation)
        assertEquals("x24442", Chord.B.representation)
    }

    @Test
    fun testAllQualities() {
        assertEquals(ChordQuality.MAJOR, Chord.fromString("C").quality)
        assertEquals(ChordQuality.MINOR, Chord.fromString("Cm").quality)
        assertEquals(ChordQuality.DOMINANT_7, Chord.fromString("C7").quality)
        assertEquals(ChordQuality.MINOR_7, Chord.fromString("Cm7").quality)
        assertEquals(ChordQuality.MAJOR_7, Chord.fromString("Cmaj7").quality)
        assertEquals(ChordQuality.SUS_4, Chord.fromString("Csus4").quality)
    }

    @Test
    fun testSus4Representations() {
        assertEquals("x35563", Chord.fromString("Csus4").representation)
        assertEquals("x46674", Chord.fromString("C#sus4").representation)
        assertEquals("xx0233", Chord.fromString("Dsus4").representation)
        assertEquals("x68896", Chord.fromString("D#sus4").representation)
        assertEquals("022200", Chord.fromString("Esus4").representation)
        assertEquals("133311", Chord.fromString("Fsus4").representation)
        assertEquals("244422", Chord.fromString("F#sus4").representation)
        assertEquals("355533", Chord.fromString("Gsus4").representation)
        assertEquals("466644", Chord.fromString("G#sus4").representation)
        assertEquals("x02230", Chord.fromString("Asus4").representation)
        assertEquals("x13341", Chord.fromString("A#sus4").representation)
        assertEquals("x24452", Chord.fromString("Bsus4").representation)
    }

    @Test
    fun testOtherQualitiesRepresentations() {
        assertEquals("x32310", Chord.fromString("C7").representation)
        assertEquals("x35343", Chord.fromString("Cm7").representation)
        assertEquals("x32000", Chord.fromString("Cmaj7").representation)
        assertEquals("242322", Chord.fromString("F#7").representation)
        assertEquals("x02010", Chord.fromString("Am7").representation)
        assertEquals("x24342", Chord.fromString("Bmaj7").representation)
    }
}
