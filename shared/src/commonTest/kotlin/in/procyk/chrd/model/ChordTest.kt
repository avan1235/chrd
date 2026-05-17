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
}
