package `in`.procyk.chrd.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM favorite_songs")
    fun observeAll(): Flow<List<SongEntity>>

    @Query("SELECT * FROM favorite_songs WHERE source = :source LIMIT 1")
    fun observeBySource(source: String): Flow<SongEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE source = :source)")
    fun isFavorite(source: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(entity: SongEntity)

    @Query("DELETE FROM favorite_songs WHERE source = :source")
    suspend fun deleteBySource(source: String)
}
