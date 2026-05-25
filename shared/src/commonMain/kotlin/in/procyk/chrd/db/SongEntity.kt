package `in`.procyk.chrd.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "favorite_songs")
data class SongEntity(
    @PrimaryKey val source: String,
    val author: String,
    val title: String,
    val contentJson: String,
    val listingJson: String,
)
