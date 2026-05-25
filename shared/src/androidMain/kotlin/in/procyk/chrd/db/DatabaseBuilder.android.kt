package `in`.procyk.chrd.db

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Composable
actual fun rememberAppDatabase(): AppDatabase {
    val context = LocalContext.current.applicationContext
    return remember(context) { buildAppDatabase(context) }
}

private fun buildAppDatabase(context: Context): AppDatabase {
    val dbFile = context.getDatabasePath(APP_DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
        .addMigrations(MIGRATION_1_2)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
