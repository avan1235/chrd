package `in`.procyk.chrd.model

import `in`.procyk.chrd.shared.ChrdSharedConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UltimateSongsOriginTest {

    @Test
    fun testFindAndParseSong() = runTest {
        val origin = UltimateSongsOrigin(ChrdSharedConfig.ULTIMATE_SONGS_ORIGIN_URL)
        val phrase = "to juz jest koniec"
        val listings = origin.find(phrase)

        val listing = listings.find { it.title.contains("To Już Jest Koniec", ignoreCase = true) }
        assertNotNull(listing, "Song listing not found for phrase: $phrase")
        assertEquals("Elektryczne Gitary", listing.author)

        val song = origin.parseSong(listing)

        val expectedSong = song("Elektryczne Gitary", "To Już Jest Koniec") {
            c {
                l {
                    +"To juz jest "
                    "konie" c "C"
                    "c, nie ma " c "G"
                    "już nic" c "Am"
                    cw("F")
                }
                l {
                    +"Jesteśmy"
                    " woln" c "C"
                    "i, może" c "G"
                    "my iś" c "Am"
                    "ć" c "F"
                }
                l {
                    +"To juz jest "
                    "konie" c "C"
                    "c, nie ma " c "G"
                    "już nic" c "Am"
                    cw("F")
                }
                l {
                    +"Jesteśmy"
                    " woln" c "C"
                    "i, bo nie ma " c "G"
                    "już ni" c "Am"
                    "c" c "F"
                }
            }
            v {
                l {
                    +"Roba"
                    "czek w swej dziurce jak docent za biurkiem" c "C"
                    cw("G")
                }
                l {
                    +"i "
                    "pszczółka na kwiatkach jak kontrol w tramwaja" c "Am"
                    "ch." c "F"
                }
                l {
                    +"Tak dłu"
                    "bie i gmera, napisze, wymyśl" c "C"
                    "i," c "G"
                }
                l {
                    +"Obe"
                    "jdzie wokoło, zabrudzi, wyczyśc" c "Am"
                    "i." c "F"
                }
                l {
                    +"I "
                    "krzaczek przy drodze i brat przy mas" c "C"
                    "zynie," c "G"
                }
                l {
                    +"Jak nog"
                    "a w skarpecie sprzedawca w kantynie." c "Am"
                    cw("F")
                }
                l {
                    +"Kamyc"
                    "zek na polu i strażnik na stra" c "C"
                    "ży," c "G"
                }
                l {
                    +"Lodó"
                    "wka wciąż ziębi kuchenka wciąż parzy" c "Am"
                    "." c "F"
                }
                l {
                    +"A po"
                    " co, a po co tak dłubie i dłu" c "C"
                    "bie," c "G"
                }
                l {
                    +"a z"
                    "a co, a za co tak myśli i skubi" c "Am"
                    "e?" c "F"
                }
                l {
                    +"I "
                    "tak sie przykłada i mówi z ekr" c "C"
                    "anu" c "G"
                }
                l {
                    +"i "
                    "bredzi latami wieczorem i ra" c "Am"
                    "no." c "F"
                }
            }
            c {
                l {
                    +"To juz jest "
                    "konie" c "C"
                    "c, nie ma " c "G"
                    "już nic" c "Am"
                    cw("F")
                }
                l {
                    +"Jesteśmy"
                    " woln" c "C"
                    "i, może" c "G"
                    "my iś" c "Am"
                    "ć" c "F"
                }
                l {
                    +"To juz jest "
                    "konie" c "C"
                    "c, nie ma " c "G"
                    "już nic" c "Am"
                    cw("F")
                }
                l {
                    +"Jesteśmy"
                    " woln" c "C"
                    "i, bo nie ma " c "G"
                    "już ni" c "Am"
                    "c" c "F"
                }
                l {
                    +"To juz jest "
                    "konie" c "C"
                    "c, nie ma " c "G"
                    "już nic" c "Am"
                    cw("F")
                }
                l {
                    +"Jesteśmy"
                    " woln" c "C"
                    "i, może" c "G"
                    "my iś" c "Am"
                    "ć" c "F"
                }
                l {
                    +"To juz jest "
                    "konie" c "C"
                    "c, nie ma " c "G"
                    "już nic" c "Am"
                    cw("F")
                }
                l {
                    +"Jesteśmy"
                    " woln" c "C"
                    "i, bo nie ma " c "G"
                    "już ni" c "Am"
                    "c" c "F"
                }
            }
        }

        assertEquals(expectedSong, song)
    }
}
