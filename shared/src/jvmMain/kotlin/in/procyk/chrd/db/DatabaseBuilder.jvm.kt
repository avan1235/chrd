package `in`.procyk.chrd.db

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers

@Composable
actual fun rememberAppDatabase(): AppDatabase {
    return remember { buildAppDatabase() }
}

private fun buildAppDatabase(): AppDatabase {
    val dbFile = File(appDataDir(), APP_DATABASE_NAME)
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
        factory = { AppDatabaseConstructor.initialize() },
    )
        .addMigrations(*MIGRATIONS)
        .fallbackToDestructiveMigration()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

private fun appDataDir(): File {
    val home = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir") ?: "."
    val os = (System.getProperty("os.name") ?: "").lowercase()
    return when {
        os.contains("mac") -> File(home, "Library/Application Support/Chrd")
        os.contains("win") ->
            File(System.getenv("APPDATA") ?: File(home, "AppData/Roaming").absolutePath, "Chrd")
        else -> File(System.getenv("XDG_DATA_HOME") ?: File(home, ".local/share").absolutePath, "Chrd")
    }
}
