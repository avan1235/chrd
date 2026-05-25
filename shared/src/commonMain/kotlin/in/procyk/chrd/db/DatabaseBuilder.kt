package `in`.procyk.chrd.db

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
expect fun rememberAppDatabase(): AppDatabase

@Composable
fun rememberAppSettingsRepository(): AppSettingsRepository {
    val db = rememberAppDatabase()
    return remember(db) { AppSettingsRepository(db.appSettingsDao()) }
}

@Composable
fun rememberSongRepository(): SongRepository {
    val db = rememberAppDatabase()
    return remember(db) { SongRepository(db.songDao()) }
}
