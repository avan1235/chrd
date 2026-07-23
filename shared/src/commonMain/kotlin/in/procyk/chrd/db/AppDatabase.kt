package `in`.procyk.chrd.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import `in`.procyk.chrd.useLiquidNavigationDefault

@Database(
    entities = [AppSettingsEntity::class, SongEntity::class],
    version = 3,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao

    abstract fun songDao(): SongDao
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `favorite_songs` (`source` TEXT NOT NULL, `author` TEXT NOT NULL, `title` TEXT NOT NULL, `contentJson` TEXT NOT NULL, `listingJson` TEXT NOT NULL, PRIMARY KEY(`source`))")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE `app_settings` " +
                    "ADD COLUMN useLiquidNavigation " +
                    "INTEGER NOT NULL DEFAULT(${if (useLiquidNavigationDefault) "1" else "0"})",
        )
    }
}

val MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
)

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

const val APP_DATABASE_NAME: String = "chrd_settings.db"
