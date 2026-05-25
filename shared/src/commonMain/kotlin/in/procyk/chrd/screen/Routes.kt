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
    data object Favorites : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data class SongDetails(
        val listing: SongListing,
    ) : Screen()

    companion object {
        val SavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Search::class, Search.serializer())
                    subclass(Favorites::class, Favorites.serializer())
                    subclass(Settings::class, Settings.serializer())
                    subclass(SongDetails::class, SongDetails.serializer())
                }
            }
        }
    }
}
