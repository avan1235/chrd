package `in`.procyk.chrd.model

import `in`.procyk.chrd.shared.ChrdSharedConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SubversionSongsOriginTest {

    @Test
    fun testFindAndParseSong() = runTest {
        val origin = SubversionSongsOrigin(ChrdSharedConfig.SUBVERSION_PL_SONGS_ORIGIN_URL)
        val phrase = "to jest juz koniec"
        val listings = origin.find(phrase)

        val listing = listings.find { it.title.contains("To już jest koniec", ignoreCase = true) }
        assertNotNull(listing, "Song listing not found for phrase: $phrase")
        assertEquals("Elektryczne Gitary", listing.author)

        val song = origin.parseSong(listing)

        val expectedSong = song("Elektryczne Gitary", "To już jest koniec") {
            c {
                l {
                    +"To już jest ko"
                    "niec" c "G"
                    +", "
                    "nie" c "D"
                    +" ma już n"
                    "ic" c "e"
                    +"  "
                    cw("C")
                }
                l {
                    +"Jesteśmy wo"
                    "lni" c "G"
                    +", "
                    "możemy" c "D"
                    +" i"
                    "ść" c "e"
                    +" "
                    cw("C")
                }
                l {
                    +"To już jest k"
                    "oniec" c "G"
                    +","
                    " możemy" c "D"
                    +" i"
                    "ść" c "e"
                    +" "
                    cw("C")
                }
                l {
                    +"Jesteśmy w"
                    "olni" c "G"
                    +","
                    " bo" c "D"
                    +" nie ma już n"
                    "ic" c "e"
                    +" "
                    cw("C")
                }
            }
            v {
                l {
                    +"Ro"
                    "baczek" c "G"
                    +" w swej dziurce jak d"
                    "ocent" c "D"
                    +" za biurkiem"
                }
                l {
                    +"i ps"
                    "zczółka" c "e"
                    +" na kwiatkach jak k"
                    "ontrol" c "C"
                    +" w tramwajach"
                }
                l {
                    +"Tak "
                    "dłubie" c "G"
                    +" i gmera, nap"
                    "isze" c "D"
                    +", wymyśli"
                }
                l {
                    +"Ob"
                    "ejdzie" c "e"
                    +" wokoło, zab"
                    "rudzi" c "C"
                    +", wyczyści"
                }
                l {
                    +"I "
                    "krzaczek" c "G"
                    +" przy drodze i "
                    "brat" c "D"
                    +" przy maszynie"
                }
                l {
                    +"Jak "
                    "noga" c "e"
                    +" w skarpecie sprze"
                    "dawca" c "C"
                    +" w kantynie"
                }
                l {
                    +"Ka"
                    "myczek" c "G"
                    +" na polu i s"
                    "trażnik" c "D"
                    +" na straży"
                }
                l {
                    +"Lo"
                    "dówka" c "e"
                    +" wciąż ziębi kuc"
                    "henka" c "C"
                    +" wciąż parzy"
                }
                l {
                    +"A "
                    "po" c "G"
                    +" co, a po co tak d"
                    "łubie" c "D"
                    +" i dłubie"
                }
                l {
                    +"a "
                    "za" c "e"
                    +" co, a za co tak m"
                    "yśli" c "C"
                    +" i skubie"
                }
                l {
                    +"I "
                    "tak" c "G"
                    +" się przykłada i "
                    "mówi" c "D"
                    +" z ekranu"
                }
                l {
                    +"i "
                    "bredzi" c "e"
                    +" latami wie"
                    "czorem" c "C"
                    +" i rano"
                }
            }
            c {
                l {
                    +"To już jest ko"
                    "niec" c "G"
                    +", "
                    "nie" c "D"
                    +" ma już n"
                    "ic" c "e"
                    +"  "
                    cw("C")
                }
                l {
                    +"Jesteśmy wo"
                    "lni" c "G"
                    +", "
                    "możemy" c "D"
                    +" i"
                    "ść" c "e"
                    +" "
                    cw("C")
                }
                l {
                    +"To już jest k"
                    "oniec" c "G"
                    +","
                    " możemy" c "D"
                    +" i"
                    "ść" c "e"
                    +" "
                    cw("C")
                }
                l {
                    +"Jesteśmy w"
                    "olni" c "G"
                    +","
                    " bo" c "D"
                    +" nie ma już n"
                    "ic" c "e"
                    +" "
                    cw("C")
                }
            }
        }

        assertEquals(expectedSong, song)
    }
}
