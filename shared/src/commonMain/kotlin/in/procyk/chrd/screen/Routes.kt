package `in`.procyk.chrd.screen

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import `in`.procyk.chrd.model.SongListing
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed class Screen : NavKey {

    @Serializable
    data object Search : Screen()

    @Serializable
    data class SongDetails(
        val listing: SongListing,
    ) : Screen()

    companion object {
        val SavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Search::class, Search.serializer())
                    subclass(SongDetails::class, SongDetails.serializer())
                }
            }
        }
    }
}
