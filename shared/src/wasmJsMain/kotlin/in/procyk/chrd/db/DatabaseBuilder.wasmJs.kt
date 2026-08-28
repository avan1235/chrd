package `in`.procyk.chrd.db

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.w3c.dom.Worker

@Composable
actual fun rememberAppDatabase(): AppDatabase {
    return remember { buildAppDatabase() }
}

private fun buildAppDatabase(): AppDatabase {
    return Room.databaseBuilder<AppDatabase>(
        name = APP_DATABASE_NAME,
        factory = { AppDatabaseConstructor.initialize() },
    )
        .addMigrations(*MIGRATIONS)
        .fallbackToDestructiveMigration()
        .setDriver(WebWorkerSQLiteDriver(createSQLiteWorker()))
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    "() => new Worker(new URL('sqlite-wasm-worker/worker.js', import.meta.url), { type: 'module' })"
)
private external fun createSQLiteWorker(): Worker
