package `in`.procyk.chrd.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = :id LIMIT 1")
    fun observe(id: Int = AppSettingsEntity.SINGLETON_ID): Flow<AppSettingsEntity?>

    @Upsert
    suspend fun upsert(entity: AppSettingsEntity)
}
